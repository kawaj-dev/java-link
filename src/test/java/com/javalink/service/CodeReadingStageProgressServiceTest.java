package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.service.CodeReadingStageProgressService.StageLearningStatus;
import com.javalink.service.CodeReadingStageProgressService.StageProgress;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeReadingStageProgressServiceTest {

    private final CodeReadingLessonCatalog lessonCatalog =
            new CodeReadingLessonCatalog();

    @Mock
    private LessonProgressService lessonProgressService;

    @Mock
    private CodeReadingFlowService flowService;

    @Mock
    private LearningProgressRestoreService restoreService;

    @Mock
    private HttpSession session;

    private CodeReadingStageProgressService service;

    @BeforeEach
    void setUp() {
        service = new CodeReadingStageProgressService(
                lessonCatalog,
                lessonProgressService,
                flowService,
                restoreService
        );
        when(restoreService.restoreAllIfAuthenticated(
                session,
                lessonCatalog.getLessons().stream().map(Lesson::id).toList()
        )).thenReturn(Map.of());
        lessonCatalog.getLessons().forEach(lesson -> {
            when(lessonProgressService.findStoredProgress(session, lesson.id()))
                    .thenReturn(Optional.empty());
            when(flowService.findStoredState(session, lesson.id()))
                    .thenReturn(Optional.empty());
        });
    }

    @Test
    void 進捗がないStageは未学習0パーセントになる() {
        Map<String, StageProgress> result = service.getStageProgresses(session);

        result.values().forEach(progress -> {
            assertEquals(StageLearningStatus.NOT_STARTED, progress.status());
            assertEquals(0, progress.percentage());
        });
    }

    @Test
    void 学習開始直後は完了Stepがなくても学習中になる() {
        Lesson lesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE1_LESSON_ID
        );
        LessonProgress progress = initialProgress(lesson);
        when(lessonProgressService.findStoredProgress(session, lesson.id()))
                .thenReturn(Optional.of(progress));
        when(flowService.findStoredState(session, lesson.id()))
                .thenReturn(Optional.of(new CodeReadingFlowState(
                        lesson.id(),
                        CodeReadingPhase.LEARNING
                )));

        StageProgress result = service.getStageProgresses(session).get(lesson.id());

        assertEquals(StageLearningStatus.IN_PROGRESS, result.status());
        assertEquals(0, result.percentage());
    }

    @Test
    void 有効な完了Stepだけを数えて進捗率の小数点以下を切り捨てる() {
        Lesson lesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE1_LESSON_ID
        );
        LessonProgress progress = initialProgress(lesson);
        progress.completeStep(lesson.steps().get(0).id());
        progress.completeStep("unknown-step");
        when(lessonProgressService.findStoredProgress(session, lesson.id()))
                .thenReturn(Optional.of(progress));

        StageProgress result = service.getStageProgresses(session).get(lesson.id());

        assertEquals(StageLearningStatus.IN_PROGRESS, result.status());
        assertEquals(100 / lesson.steps().size(), result.percentage());
    }

    @Test
    void 全Step完了でもStage未完了なら学習中100パーセントになる() {
        Lesson lesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE1_LESSON_ID
        );
        LessonProgress progress = initialProgress(lesson);
        lesson.steps().forEach(step -> progress.completeStep(step.id()));
        when(lessonProgressService.findStoredProgress(session, lesson.id()))
                .thenReturn(Optional.of(progress));

        StageProgress result = service.getStageProgresses(session).get(lesson.id());

        assertEquals(StageLearningStatus.IN_PROGRESS, result.status());
        assertEquals(100, result.percentage());
    }

    @Test
    void Stage完了フラグを優先して学習完了100パーセントになる() {
        Lesson lesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE1_LESSON_ID
        );
        LessonProgress progress = initialProgress(lesson);
        progress.setCompleted(true);
        when(lessonProgressService.findStoredProgress(session, lesson.id()))
                .thenReturn(Optional.of(progress));

        StageProgress result = service.getStageProgresses(session).get(lesson.id());

        assertEquals(StageLearningStatus.COMPLETED, result.status());
        assertEquals(100, result.percentage());
    }

    @Test
    void セッション進捗がなければログインユーザーの復元済み進捗を表示する() {
        Lesson lesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE2_LESSON_ID
        );
        LessonProgress restored = initialProgress(lesson);
        restored.completeStep(lesson.steps().get(0).id());
        when(restoreService.restoreAllIfAuthenticated(
                session,
                lessonCatalog.getLessons().stream().map(Lesson::id).toList()
        )).thenReturn(Map.of(lesson.id(), restored));

        StageProgress result = service.getStageProgresses(session).get(lesson.id());

        assertEquals(StageLearningStatus.IN_PROGRESS, result.status());
        assertEquals(100 / lesson.steps().size(), result.percentage());
    }

    @Test
    void リセット後の初期進捗は未学習になり他Stageの進捗は維持する() {
        Lesson resetLesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE1_LESSON_ID
        );
        Lesson learningLesson = lessonCatalog.getLesson(
                CodeReadingLessonCatalog.STAGE2_LESSON_ID
        );
        when(lessonProgressService.findStoredProgress(session, resetLesson.id()))
                .thenReturn(Optional.of(initialProgress(resetLesson)));
        when(flowService.findStoredState(session, resetLesson.id()))
                .thenReturn(Optional.of(CodeReadingFlowState.initial(
                        resetLesson.id()
                )));
        LessonProgress learningProgress = initialProgress(learningLesson);
        learningProgress.completeStep(learningLesson.steps().get(0).id());
        when(lessonProgressService.findStoredProgress(
                session,
                learningLesson.id()
        )).thenReturn(Optional.of(learningProgress));

        Map<String, StageProgress> result = service.getStageProgresses(session);

        assertEquals(
                StageLearningStatus.NOT_STARTED,
                result.get(resetLesson.id()).status()
        );
        assertEquals(
                StageLearningStatus.IN_PROGRESS,
                result.get(learningLesson.id()).status()
        );
    }

    private LessonProgress initialProgress(Lesson lesson) {
        return new LessonProgress(lesson.id(), lesson.steps().get(0).id());
    }
}
