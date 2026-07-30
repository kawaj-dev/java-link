package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.LessonViewModel;
import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 教材と進捗から画面表示用の値を正しく計算できるか確認します。
 */
class LessonViewModelServiceTest {

    private static final String LESSON_ID = "main-method-basic";

    private Lesson lesson;
    private LessonProgress progress;
    private LessonService lessonService;
    private LessonViewModelService viewModelService;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        List<LessonStep> steps = new ArrayList<>();
        steps.add(createStep("public", 1, true));
        steps.add(createStep("static", 2, true));
        steps.add(createStep("void", 3, true));
        steps.add(createStep("main", 4, true));
        steps.add(createStep("string-array", 5, true));
        steps.add(createStep("args", 6, true));
        steps.add(createStep("hint", 7, false));

        lesson = new Lesson(
                LESSON_ID,
                "mainメソッドを完成させよう",
                "mainメソッドを学習します。",
                "public static void main(String[] args){}",
                steps
        );
        progress = new LessonProgress(LESSON_ID, "public");
        session = new MockHttpSession();

        lessonService = mock(LessonService.class);
        LessonProgressService progressService =
                mock(LessonProgressService.class);
        when(lessonService.getLesson(LESSON_ID)).thenReturn(lesson);
        when(lessonService.getStep(LESSON_ID, "public"))
                .thenReturn(steps.get(0));
        when(progressService.getProgress(session, LESSON_ID))
                .thenReturn(progress);

        viewModelService = new LessonViewModelService(
                lessonService,
                progressService
        );
    }

    @Test
    void 初期状態は進捗ゼロで各booleanもfalseになる() {
        LessonViewModel viewModel =
                viewModelService.createViewModel(session, LESSON_ID);

        assertEquals(0, viewModel.completedCount());
        assertEquals(6, viewModel.totalCount());
        assertEquals(0, viewModel.progressPercent());
        assertFalse(viewModel.codeComplete());
        assertFalse(viewModel.runEnabled());
        assertFalse(viewModel.consoleVisible());
    }

    @Test
    void 六ステップ中一ステップ完了は17パーセントになる() {
        progress.completeStep("public");

        LessonViewModel viewModel =
                viewModelService.createViewModel(session, LESSON_ID);

        assertEquals(1, viewModel.completedCount());
        assertEquals(17, viewModel.progressPercent());
        assertFalse(viewModel.codeComplete());
        assertFalse(viewModel.runEnabled());
    }

    @Test
    void 全必須ステップ完了で100パーセントかつ実行可能になる() {
        completeAllRequiredSteps();

        LessonViewModel viewModel =
                viewModelService.createViewModel(session, LESSON_ID);

        assertEquals(6, viewModel.completedCount());
        assertEquals(100, viewModel.progressPercent());
        assertTrue(viewModel.codeComplete());
        assertTrue(viewModel.runEnabled());
    }

    @Test
    void programExecutedがtrueならコンソールを表示する() {
        progress.setProgramExecuted(true);

        assertTrue(
                viewModelService.createViewModel(session, LESSON_ID)
                        .consoleVisible()
        );
    }

    @Test
    void 必須でないステップは分母にも完了数にも含めない() {
        progress.completeStep("hint");

        LessonViewModel viewModel =
                viewModelService.createViewModel(session, LESSON_ID);

        assertEquals(6, viewModel.totalCount());
        assertEquals(0, viewModel.completedCount());
        assertEquals(0, viewModel.progressPercent());
    }

    @Test
    void 別教材の完了IDが混ざってもCodeCompleteにならない() {
        progress.completeStep("foreign-public");
        progress.completeStep("foreign-static");
        progress.completeStep("foreign-void");
        progress.completeStep("foreign-main");
        progress.completeStep("foreign-string-array");
        progress.completeStep("foreign-args");

        LessonViewModel viewModel =
                viewModelService.createViewModel(session, LESSON_ID);

        assertEquals(0, viewModel.completedCount());
        assertFalse(viewModel.codeComplete());
        assertFalse(viewModel.runEnabled());
    }

    @Test
    void 必須ステップがゼロなら進捗率ゼロで未完了になる() {
        Lesson optionalOnlyLesson = new Lesson(
                "optional-only",
                "任意ステップだけの教材",
                "ゼロ除算を確認します。",
                "",
                List.of(createStep("optional", 1, false))
        );
        LessonProgress optionalProgress =
                new LessonProgress("optional-only", "optional");
        LessonProgressService progressService =
                mock(LessonProgressService.class);
        when(lessonService.getLesson("optional-only"))
                .thenReturn(optionalOnlyLesson);
        when(lessonService.getStep("optional-only", "optional"))
                .thenReturn(optionalOnlyLesson.steps().get(0));
        when(progressService.getProgress(session, "optional-only"))
                .thenReturn(optionalProgress);
        LessonViewModelService service =
                new LessonViewModelService(lessonService, progressService);

        LessonViewModel viewModel =
                service.createViewModel(session, "optional-only");

        assertEquals(0, viewModel.totalCount());
        assertEquals(0, viewModel.progressPercent());
        assertFalse(viewModel.codeComplete());
    }

    @Test
    void 存在しない教材IDでは例外になる() {
        when(lessonService.getLesson("missing"))
                .thenThrow(new IllegalArgumentException(
                        "教材が見つかりません。lessonId: missing"
                ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> viewModelService.createViewModel(session, "missing")
        );

        assertTrue(exception.getMessage().contains("missing"));
    }

    private void completeAllRequiredSteps() {
        lesson.steps().stream()
                .filter(LessonStep::required)
                .map(LessonStep::id)
                .forEach(progress::completeStep);
    }

    private LessonStep createStep(
            String stepId,
            int order,
            boolean required
    ) {
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
                required
        );
    }
}
