package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.ProgramRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LessonRunServiceTest {

    private static final String LESSON_ID = "main-method-basic";

    private LessonService lessonService;
    private LessonProgressService lessonProgressService;
    private LessonRunService lessonRunService;

    @BeforeEach
    void setUp() {
        QuizService quizService = new QuizService();
        CodeReadingLessonCatalog lessonCatalog =
                new CodeReadingLessonCatalog();
        lessonService = new LessonService(quizService, lessonCatalog);
        lessonProgressService = new LessonProgressService(lessonService);
        lessonRunService = new LessonRunService(
                lessonService,
                lessonProgressService,
                lessonCatalog
        );
    }

    @Test
    void 未完了では実行せず実行済みにしない() {
        MockHttpSession session = new MockHttpSession();

        ProgramRunResult result =
                lessonRunService.runLesson(session, LESSON_ID);
        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);

        assertFalse(result.success());
        assertFalse(progress.isProgramExecuted());
    }

    @Test
    void 全必須ステップ完了後は固定の成功結果を返してSessionへ保存する() {
        MockHttpSession session = completedSession();
        LessonProgress beforeRun =
                lessonProgressService.getProgress(session, LESSON_ID);

        ProgramRunResult result =
                lessonRunService.runLesson(session, LESSON_ID);
        LessonProgress stored =
                lessonProgressService.getProgress(session, LESSON_ID);

        assertTrue(result.success());
        assertFalse(result.consoleOutput().isBlank());
        assertFalse(result.message().isBlank());
        assertTrue(stored.isProgramExecuted());
        assertSame(beforeRun, stored);
    }

    @Test
    void 別教材の完了IDだけでは実行できない() {
        MockHttpSession session = new MockHttpSession();
        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);
        progress.completeStep("another-lesson-step");
        lessonProgressService.saveProgress(session, LESSON_ID, progress);

        ProgramRunResult result =
                lessonRunService.runLesson(session, LESSON_ID);

        assertFalse(result.success());
        assertFalse(progress.isProgramExecuted());
    }

    @Test
    void 存在しない教材IDでは例外になる() {
        MockHttpSession session = new MockHttpSession();

        assertThrows(
                IllegalArgumentException.class,
                () -> lessonRunService.runLesson(session, "missing-lesson")
        );
    }

    private MockHttpSession completedSession() {
        MockHttpSession session = new MockHttpSession();
        Lesson lesson = lessonService.getLesson(LESSON_ID);
        for (LessonStep step : lesson.steps()) {
            if (step.required()) {
                lessonProgressService.addCompletedStep(
                        session,
                        LESSON_ID,
                        step.id()
                );
            }
        }
        return session;
    }
}
