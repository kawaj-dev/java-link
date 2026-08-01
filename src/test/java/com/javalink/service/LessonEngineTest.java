package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 回答後のLessonProgress更新を確認します。
 */
class LessonEngineTest {

    private static final String LESSON_ID = "main-method-basic";

    private MockHttpSession session;
    private LessonService lessonService;
    private LessonProgressService progressService;
    private LessonEngine lessonEngine;

    @BeforeEach
    void setUp() {
        QuizService quizService = new QuizService();
        lessonService = new LessonService(
                quizService,
                new CodeReadingLessonCatalog()
        );
        progressService = new LessonProgressService(lessonService);
        lessonEngine = new LessonEngine(
                lessonService,
                progressService,
                quizService
        );
        session = new MockHttpSession();
    }

    @Test
    void 正解で回答状態を保存し完了追加して現在ステップに留まる() {
        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "accessible"
                );

        LessonProgress progress = result.progress();
        assertTrue(result.correct());
        assertEquals("accessible", progress.getSelectedOptionId());
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());
        assertTrue(progress.isStepCompleted("public"));
        assertEquals("public", progress.getCurrentStepId());
        assertEquals("static", result.nextStep().orElseThrow().id());
        assertEquals("外から使える", result.correctOption().text());
    }

    @Test
    void 正しい意味カードを置くと完了を保存して次の用語へ進む() {
        LessonEngine.AnswerResult result =
                lessonEngine.placeMeaningCard(
                        session,
                        LESSON_ID,
                        "accessible"
                );

        assertTrue(result.correct());
        assertTrue(result.progress().isStepCompleted("public"));
        assertEquals("static", result.progress().getCurrentStepId());
        assertFalse(result.progress().isAnswered());
        assertFalse(result.progress().isCorrect());
        assertEquals("", result.progress().getSelectedOptionId());
    }

    @Test
    void 順番の違う意味カードでは現在の用語に留まる() {
        LessonEngine.AnswerResult result =
                lessonEngine.placeMeaningCard(
                        session,
                        LESSON_ID,
                        "without-instance"
                );

        assertFalse(result.correct());
        assertEquals("public", result.progress().getCurrentStepId());
        assertTrue(result.progress().isAnswered());
        assertFalse(result.progress().isCorrect());
        assertTrue(result.progress().getCompletedStepIds().isEmpty());
    }

    @Test
    void 不正解では完了追加も次ステップ移動もしない() {
        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "repeat"
                );

        LessonProgress progress = result.progress();
        assertFalse(result.correct());
        assertEquals("repeat", progress.getSelectedOptionId());
        assertTrue(progress.isAnswered());
        assertFalse(progress.isCorrect());
        assertTrue(progress.getCompletedStepIds().isEmpty());
        assertEquals("public", progress.getCurrentStepId());
        assertTrue(result.nextStep().isEmpty());
    }

    @Test
    void 最後の必須ステップ正解で教材完了になる() {
        Lesson lesson = lessonService.getLesson(LESSON_ID);
        for (LessonStep step : lesson.steps()) {
            if (!step.id().equals("args")) {
                progressService.addCompletedStep(
                        session,
                        LESSON_ID,
                        step.id()
                );
            }
        }
        progressService.updateCurrentStep(session, LESSON_ID, "args");

        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "argument-variable"
                );

        assertTrue(result.correct());
        assertTrue(result.progress().isStepCompleted("args"));
        assertTrue(result.progress().isCompleted());
        assertTrue(result.nextStep().isEmpty());
    }

    @Test
    void 更新後の進捗はセッションMapへ保存される() {
        LessonProgress resultProgress =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "accessible"
                ).progress();

        @SuppressWarnings("unchecked")
        Map<String, LessonProgress> stored =
                (Map<String, LessonProgress>) session.getAttribute(
                        LessonProgressService.LESSON_PROGRESS_MAP
                );

        assertEquals(resultProgress, stored.get(LESSON_ID));
    }

    @Test
    void 存在しない教材IDでは例外になる() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonEngine.answerCurrentStep(
                        session,
                        "missing-lesson",
                        "anything"
                )
        );

        assertTrue(exception.getMessage().contains("missing-lesson"));
    }

    @Test
    void 存在しない現在ステップIDでは例外になる() {
        LessonProgress progress =
                progressService.getProgress(session, LESSON_ID);
        progress.setCurrentStepId("missing-step");
        progressService.saveProgress(session, LESSON_ID, progress);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "anything"
                )
        );

        assertTrue(exception.getMessage().contains("missing-step"));
    }

    @Test
    void 存在しない選択肢IDは不正解として保存する() {
        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "missing-option"
                );

        assertFalse(result.correct());
        assertEquals(
                "missing-option",
                result.progress().getSelectedOptionId()
        );
        assertTrue(result.progress().isAnswered());
        assertFalse(result.progress().isCorrect());
        assertTrue(result.progress().getCompletedStepIds().isEmpty());
    }

    @Test
    void 正解確認後に次ステップへ進み回答状態をリセットする() {
        lessonEngine.answerCurrentStep(
                session,
                LESSON_ID,
                "accessible"
        );

        LessonProgress progress =
                lessonEngine.moveToNextStep(session, LESSON_ID);

        assertEquals("static", progress.getCurrentStepId());
        assertFalse(progress.isAnswered());
        assertFalse(progress.isCorrect());
        assertEquals("", progress.getSelectedOptionId());
        assertTrue(progress.isStepCompleted("public"));
    }

    @Test
    void 不正解状態で次へ進もうとしても現在ステップを維持する() {
        lessonEngine.answerCurrentStep(
                session,
                LESSON_ID,
                "repeat"
        );

        LessonProgress progress =
                lessonEngine.moveToNextStep(session, LESSON_ID);

        assertEquals("public", progress.getCurrentStepId());
        assertTrue(progress.isAnswered());
        assertFalse(progress.isCorrect());
        assertEquals("repeat", progress.getSelectedOptionId());
    }

    @Test
    void 未回答状態で次へ進もうとしても現在ステップを維持する() {
        LessonProgress progress =
                lessonEngine.moveToNextStep(session, LESSON_ID);

        assertEquals("public", progress.getCurrentStepId());
        assertFalse(progress.isAnswered());
    }

    @Test
    void 最終ステップ正解後に次へ進もうとしてもargsに留まる() {
        Lesson lesson = lessonService.getLesson(LESSON_ID);
        for (LessonStep step : lesson.steps()) {
            if (!step.id().equals("args")) {
                progressService.addCompletedStep(
                        session,
                        LESSON_ID,
                        step.id()
                );
            }
        }
        progressService.updateCurrentStep(session, LESSON_ID, "args");
        lessonEngine.answerCurrentStep(
                session,
                LESSON_ID,
                "argument-variable"
        );

        LessonProgress progress =
                lessonEngine.moveToNextStep(session, LESSON_ID);

        assertEquals("args", progress.getCurrentStepId());
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());
        assertEquals(
                "argument-variable",
                progress.getSelectedOptionId()
        );
    }

    @Test
    void 前へ戻っても完了済みステップと完了状態を維持する() {
        progressService.addCompletedStep(session, LESSON_ID, "public");
        progressService.updateCurrentStep(session, LESSON_ID, "static");
        progressService.updateCompleted(session, LESSON_ID, true);
        progressService.updateProgramExecuted(session, LESSON_ID, true);

        LessonProgress progress =
                lessonEngine.moveToPreviousStep(session, LESSON_ID);

        assertEquals("public", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("public"));
        assertTrue(progress.isCompleted());
        assertTrue(progress.isProgramExecuted());
        assertFalse(progress.isAnswered());
        assertEquals("", progress.getSelectedOptionId());
    }

    @Test
    void publicから前へ戻ろうとしても移動しない() {
        LessonProgress progress =
                lessonEngine.moveToPreviousStep(session, LESSON_ID);

        assertEquals("public", progress.getCurrentStepId());
    }

    @Test
    void 復習中は再回答せず次のステップへ移動できる() {
        progressService.addCompletedStep(session, LESSON_ID, "public");
        progressService.addCompletedStep(session, LESSON_ID, "static");
        progressService.updateCurrentStep(session, LESSON_ID, "static");
        lessonEngine.moveToPreviousStep(session, LESSON_ID);

        LessonProgress progress =
                lessonEngine.moveToNextStep(session, LESSON_ID);

        assertEquals("static", progress.getCurrentStepId());
        assertEquals(2, progress.getCompletedStepIds().size());
        assertFalse(progress.isAnswered());
    }

    @Test
    void 復習中に直接回答しても進捗を変更しない() {
        progressService.addCompletedStep(session, LESSON_ID, "public");
        LessonProgress before =
                progressService.getProgress(session, LESSON_ID);

        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        LESSON_ID,
                        "repeat"
                );

        assertTrue(result.correct());
        assertEquals(1, before.getCompletedStepIds().size());
        assertFalse(before.isAnswered());
        assertEquals("", before.getSelectedOptionId());
    }
}
