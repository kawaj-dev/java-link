package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HTTPセッションを使った教材別進捗管理を確認します。
 */
class LessonProgressServiceTest {

    private static final String MAIN_LESSON_ID = "main-method-basic";
    private static final String SECOND_LESSON_ID = "second-lesson";

    private MockHttpSession session;
    private LessonProgressService progressService;

    @BeforeEach
    void setUp() {
        LessonStep publicStep = createStep("public", 1);
        LessonStep staticStep = createStep("static", 2);
        LessonStep introStep = createStep("intro", 1);

        Lesson mainLesson = new Lesson(
                MAIN_LESSON_ID,
                "mainメソッドを完成させよう",
                "mainメソッドを学習します。",
                "public static void main(String[] args){}",
                List.of(publicStep, staticStep)
        );
        Lesson secondLesson = new Lesson(
                SECOND_LESSON_ID,
                "2つ目の教材",
                "進捗分離を確認する教材です。",
                "class Sample {}",
                List.of(introStep)
        );

        LessonService lessonService = mock(LessonService.class);
        when(lessonService.getLesson(MAIN_LESSON_ID)).thenReturn(mainLesson);
        when(lessonService.getLesson(SECOND_LESSON_ID)).thenReturn(secondLesson);
        when(lessonService.getLesson("missing-lesson"))
                .thenThrow(new IllegalArgumentException(
                        "教材が見つかりません。lessonId: missing-lesson"
                ));
        when(lessonService.getFirstStep(MAIN_LESSON_ID)).thenReturn(publicStep);
        when(lessonService.getFirstStep(SECOND_LESSON_ID)).thenReturn(introStep);
        when(lessonService.getStep(MAIN_LESSON_ID, "public")).thenReturn(publicStep);
        when(lessonService.getStep(MAIN_LESSON_ID, "static")).thenReturn(staticStep);
        when(lessonService.getStep(SECOND_LESSON_ID, "intro")).thenReturn(introStep);

        session = new MockHttpSession();
        progressService = new LessonProgressService(lessonService);
    }

    @Test
    void 初回取得で初期進捗を作成しセッションへ保存する() {
        LessonProgress progress =
                progressService.getProgress(session, MAIN_LESSON_ID);

        assertEquals(MAIN_LESSON_ID, progress.getLessonId());
        assertEquals("public", progress.getCurrentStepId());
        assertTrue(progress.getCompletedStepIds().isEmpty());
        assertEquals("", progress.getSelectedOptionId());
        assertFalse(progress.isAnswered());
        assertFalse(progress.isCorrect());
        assertFalse(progress.isCompleted());
        assertFalse(progress.isProgramExecuted());

        @SuppressWarnings("unchecked")
        Map<String, LessonProgress> stored =
                (Map<String, LessonProgress>) session.getAttribute(
                        LessonProgressService.LESSON_PROGRESS_MAP
                );
        assertEquals(progress, stored.get(MAIN_LESSON_ID));
    }

    @Test
    void 同じ完了ステップを二回追加しても重複しない() {
        progressService.addCompletedStep(session, MAIN_LESSON_ID, "public");
        progressService.addCompletedStep(session, MAIN_LESSON_ID, "public");

        LessonProgress progress =
                progressService.getProgress(session, MAIN_LESSON_ID);
        assertEquals(1, progress.getCompletedStepIds().size());
        assertTrue(progress.isStepCompleted("public"));
    }

    @Test
    void 現在ステップを更新できる() {
        progressService.updateCurrentStep(session, MAIN_LESSON_ID, "static");

        assertEquals(
                "static",
                progressService.getProgress(session, MAIN_LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 回答状態を更新できる() {
        progressService.updateAnswerState(
                session,
                MAIN_LESSON_ID,
                "accessible",
                true,
                true
        );

        LessonProgress progress =
                progressService.getProgress(session, MAIN_LESSON_ID);
        assertEquals("accessible", progress.getSelectedOptionId());
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());
    }

    @Test
    void 教材完了状態を更新できる() {
        progressService.updateCompleted(session, MAIN_LESSON_ID, true);

        assertTrue(
                progressService.getProgress(session, MAIN_LESSON_ID)
                        .isCompleted()
        );
    }

    @Test
    void プログラム実行状態を更新できる() {
        progressService.updateProgramExecuted(session, MAIN_LESSON_ID, true);

        assertTrue(
                progressService.getProgress(session, MAIN_LESSON_ID)
                        .isProgramExecuted()
        );
    }

    @Test
    void リセット後は新しい初期状態へ戻る() {
        LessonProgress beforeReset =
                progressService.getProgress(session, MAIN_LESSON_ID);
        progressService.addCompletedStep(session, MAIN_LESSON_ID, "public");
        progressService.updateCurrentStep(session, MAIN_LESSON_ID, "static");
        progressService.updateAnswerState(
                session,
                MAIN_LESSON_ID,
                "accessible",
                true,
                true
        );
        progressService.updateCompleted(session, MAIN_LESSON_ID, true);
        progressService.updateProgramExecuted(session, MAIN_LESSON_ID, true);

        LessonProgress afterReset =
                progressService.resetProgress(session, MAIN_LESSON_ID);

        assertNotSame(beforeReset, afterReset);
        assertEquals("public", afterReset.getCurrentStepId());
        assertTrue(afterReset.getCompletedStepIds().isEmpty());
        assertEquals("", afterReset.getSelectedOptionId());
        assertFalse(afterReset.isAnswered());
        assertFalse(afterReset.isCorrect());
        assertFalse(afterReset.isCompleted());
        assertFalse(afterReset.isProgramExecuted());
    }

    @Test
    void 同一セッションでも教材ごとに進捗を分離する() {
        progressService.addCompletedStep(session, MAIN_LESSON_ID, "public");

        LessonProgress mainProgress =
                progressService.getProgress(session, MAIN_LESSON_ID);
        LessonProgress secondProgress =
                progressService.getProgress(session, SECOND_LESSON_ID);

        assertTrue(mainProgress.isStepCompleted("public"));
        assertTrue(secondProgress.getCompletedStepIds().isEmpty());
        assertEquals("intro", secondProgress.getCurrentStepId());
    }

    @Test
    void 一つの教材をリセットしても別教材の進捗は残る() {
        progressService.addCompletedStep(session, MAIN_LESSON_ID, "public");
        progressService.addCompletedStep(session, SECOND_LESSON_ID, "intro");

        progressService.resetProgress(session, MAIN_LESSON_ID);

        assertTrue(
                progressService.getProgress(session, MAIN_LESSON_ID)
                        .getCompletedStepIds()
                        .isEmpty()
        );
        assertTrue(
                progressService.getProgress(session, SECOND_LESSON_ID)
                        .isStepCompleted("intro")
        );
    }

    @Test
    void 存在しない教材IDでは分かりやすい例外になる() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> progressService.getProgress(session, "missing-lesson")
        );

        assertTrue(exception.getMessage().contains("missing-lesson"));
    }

    private LessonStep createStep(String stepId, int order) {
        QuizQuestion question = new QuizQuestion(
                stepId,
                "",
                stepId,
                "",
                stepId + " の問題",
                List.of(new QuizOption("correct", "正解")),
                "correct"
        );

        return new LessonStep(
                stepId,
                order,
                stepId,
                question.codeBefore(),
                question.targetCode(),
                question.codeAfter(),
                question,
                true
        );
    }
}
