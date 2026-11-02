package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;
import com.javalink.model.CodeReadingPart;
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
        when(learningProgressService.findProgressSnapshot(userAccount, LESSON_ID))
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
    void 詳細形式はDBに保存された完了済みStepだけを復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("third", false);
        Lesson lesson = lesson(
                step("first", true),
                step("second", true),
                step("third", true),
                step("last", true)
        );
        stubDetailedProgress(
                userAccount,
                storedProgress,
                Set.of("first", "second")
        );
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();

        assertEquals(Set.of("first", "second"), restored.getCompletedStepIds());
        assertFalse(restored.isStepCompleted("third"));
        assertFalse(restored.isStepCompleted("last"));
        assertFalse(restored.isAnswered());
        assertFalse(restored.isCorrect());
        verify(learningProgressService)
                .findProgressSnapshot(userAccount, LESSON_ID);
        verifyNoMoreInteractions(learningProgressService);
    }

    @Test
    void 詳細形式で現在Stepが完了済みなら次のStepを操作できる() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("second", false);
        Lesson lesson = lesson(
                step("first", true),
                step("second", true),
                step("third", true)
        );
        stubDetailedProgress(
                userAccount,
                storedProgress,
                Set.of("first", "second")
        );
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();
        CodeReadingPart part = part("first", "second", "third");
        CodeReadingPartService partService = new CodeReadingPartService(
                mock(CodeReadingLessonCatalog.class)
        );

        assertTrue(restored.isAnswered());
        assertTrue(restored.isCorrect());
        assertEquals("", restored.getSelectedOptionId());
        assertEquals("third", partService.findActionableStepId(restored, part));
    }

    @Test
    void 詳細形式で未確認の現在Stepだけが操作対象になる() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("second", false);
        Lesson lesson = lesson(
                step("first", true),
                step("second", true),
                step("third", true)
        );
        stubDetailedProgress(userAccount, storedProgress, Set.of("first"));
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();
        CodeReadingPart part = part("first", "second", "third");
        CodeReadingPartService partService = new CodeReadingPartService(
                mock(CodeReadingLessonCatalog.class)
        );

        assertEquals("second", partService.findActionableStepId(restored, part));
        assertFalse(restored.isStepCompleted("third"));
    }

    @Test
    void 詳細形式はPart境界をまたいだ完了状態をそのまま復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("part2-first", false);
        Lesson lesson = lesson(
                step("part1-first", true),
                step("part1-last", true),
                step("part2-first", true),
                step("part2-last", true)
        );
        stubDetailedProgress(
                userAccount,
                storedProgress,
                Set.of("part1-first", "part1-last")
        );
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();

        assertEquals("part2-first", restored.getCurrentStepId());
        assertEquals(
                Set.of("part1-first", "part1-last"),
                restored.getCompletedStepIds()
        );
        assertFalse(restored.isStepCompleted("part2-first"));
    }

    @Test
    void 詳細形式のStep0件は最初のStepなら未完了のまま復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("first", false);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubDetailedProgress(userAccount, storedProgress, Set.of());
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();

        assertTrue(restored.getCompletedStepIds().isEmpty());
        assertFalse(restored.isAnswered());
        assertFalse(restored.isCompleted());
    }

    @Test
    void 詳細形式のStep0件で現在位置が後続Stepなら矛盾として復元しない() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("last", false);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubDetailedProgress(userAccount, storedProgress, Set.of());
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void 詳細形式にLesson未登録のStepがあれば復元しない() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("last", false);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubDetailedProgress(
                userAccount,
                storedProgress,
                Set.of("first", "unknown")
        );
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void 詳細形式のLesson完了は全requiredStepが保存済みの場合だけ復元する() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("last", true);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubDetailedProgress(
                userAccount,
                storedProgress,
                Set.of("first", "last")
        );
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        LessonProgress restored = restoreService
                .restoreIfAuthenticated(session, LESSON_ID)
                .orElseThrow();

        assertTrue(restored.isCompleted());
        assertEquals(Set.of("first", "last"), restored.getCompletedStepIds());
    }

    @Test
    void 詳細形式のLesson完了でrequiredStepが不足していれば復元しない() {
        UserAccount userAccount = mock(UserAccount.class);
        LearningProgress storedProgress = storedProgress("last", true);
        Lesson lesson = lesson(step("first", true), step("last", true));
        stubDetailedProgress(userAccount, storedProgress, Set.of("last"));
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);

        Optional<LessonProgress> result =
                restoreService.restoreIfAuthenticated(session, LESSON_ID);

        assertTrue(result.isEmpty());
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
        when(learningProgressService.findProgressSnapshot(userAccount, LESSON_ID))
                .thenReturn(Optional.of(
                        new LearningProgressService.ProgressSnapshot(
                                storedProgress,
                                Set.of(),
                                false
                        )
                ));
    }

    private void stubDetailedProgress(
            UserAccount userAccount,
            LearningProgress storedProgress,
            Set<String> completedStepIds
    ) {
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));
        when(learningProgressService.findProgressSnapshot(userAccount, LESSON_ID))
                .thenReturn(Optional.of(
                        new LearningProgressService.ProgressSnapshot(
                                storedProgress,
                                completedStepIds,
                                true
                        )
                ));
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

    private CodeReadingPart part(String... stepIds) {
        return new CodeReadingPart(
                "part-1",
                1,
                "Part 1",
                List.of(),
                "",
                List.of(stepIds),
                List.of(stepIds),
                List.of(),
                ""
        );
    }
}
