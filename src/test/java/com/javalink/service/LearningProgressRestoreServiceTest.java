package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizQuestion;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LearningProgressRestoreServiceTest {

    private static final String LESSON_ID = "stage1";

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private LearningProgressService learningProgressService;

    @Mock
    private LessonService lessonService;

    @Mock
    private HttpSession session;

    private LearningProgressRestoreService restoreService;

    @BeforeEach
    void setUp() {
        restoreService = new LearningProgressRestoreService(
                authenticatedUserService,
                learningProgressService,
                lessonService
        );
    }

    @Test
    void 未ログインならDB進捗を検索せず空を返す() {
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.empty());

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
        verifyNoInteractions(learningProgressService, lessonService);
    }

    @Test
    void ログイン済みでもDB進捗がなければ空を返す() {
        UserAccount userAccount = mock(UserAccount.class);
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));
        when(learningProgressService.findProgress(userAccount, LESSON_ID))
                .thenReturn(Optional.empty());

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
        verifyNoInteractions(lessonService);
    }

    @Test
    void 途中進捗は現在Stepより前のrequiredStepだけを完了済みにして復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("third", false);
        LessonStep first = step("first", true);
        LessonStep optional = step("optional", false);
        LessonStep third = step("third", true);
        LessonStep last = step("last", true);
        Lesson lesson = lesson(first, optional, third, last);
        stubProgress(userAccount, storedProgress);
        when(lessonService.getLesson(LESSON_ID))
                .thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isPresent());
        LessonProgress restored = result.orElseThrow();
        assertEquals(LESSON_ID, restored.getLessonId());
        assertEquals("third", restored.getCurrentStepId());
        assertEquals(Set.of("first"), restored.getCompletedStepIds());
        assertFalse(restored.isStepCompleted("optional"));
        assertFalse(restored.isStepCompleted("third"));
        assertFalse(restored.isCompleted());
        assertEquals("", restored.getSelectedOptionId());
        assertFalse(restored.isAnswered());
        assertFalse(restored.isCorrect());
        assertFalse(restored.isProgramExecuted());
    }

    @Test
    void 完了済み進捗はすべてのrequiredStepを完了済みにして復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("last", true);
        LessonStep first = step("first", true);
        LessonStep optional = step("optional", false);
        LessonStep last = step("last", true);
        Lesson lesson = lesson(first, optional, last);
        stubProgress(userAccount, storedProgress);
        when(lessonService.getLesson(LESSON_ID))
                .thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isPresent());
        LessonProgress restored = result.orElseThrow();
        assertEquals("last", restored.getCurrentStepId());
        assertEquals(Set.of("first", "last"), restored.getCompletedStepIds());
        assertFalse(restored.isStepCompleted("optional"));
        assertTrue(restored.isCompleted());
    }

    @Test
    void DBの現在StepがLessonに存在しなければ空を返す() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("missing", false);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubProgress(userAccount, storedProgress);
        when(lessonService.getLesson(LESSON_ID))
                .thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void 完了済みでも現在Stepが最後のrequiredStepでなければ空を返す() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("first", true);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubProgress(userAccount, storedProgress);
        when(lessonService.getLesson(LESSON_ID))
                .thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
    }

    private void stubProgress(
            UserAccount userAccount,
            LearningProgress storedProgress
    ) {
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));
        when(learningProgressService.findProgress(userAccount, LESSON_ID))
                .thenReturn(Optional.of(storedProgress));
    }

    private LearningProgress storedProgress(
            String currentStepId,
            boolean completed
    ) {
        LearningProgress progress = new LearningProgress();
        progress.setCurrentStepId(currentStepId);
        progress.setCompleted(completed);
        return progress;
    }

    private LessonStep step(String id, boolean required) {
        return new LessonStep(
                id,
                1,
                id,
                "",
                id,
                "",
                mock(QuizQuestion.class),
                required
        );
    }

    private Lesson lesson(LessonStep... steps) {
        return new Lesson(
                LESSON_ID,
                "Test lesson",
                "Test description",
                "",
                List.of(steps)
        );
    }
}
