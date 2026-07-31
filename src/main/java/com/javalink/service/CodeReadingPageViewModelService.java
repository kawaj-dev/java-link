package com.javalink.service;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 画面フェーズと学習進捗から、Quiz画面専用ViewModelを作ります。
 */
@Service
public class CodeReadingPageViewModelService {

    private static final String STAGE_NAME = "Stage 1";
    private static final String LEARNING_GOAL =
            "「Hello」と表示するプログラムを読めるようになろう";

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final CodeReadingFlowService flowService;
    private final CodeReadingPartService partService;

    public CodeReadingPageViewModelService(
            LessonService lessonService,
            LessonProgressService lessonProgressService,
            CodeReadingFlowService flowService,
            CodeReadingPartService partService
    ) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
        this.flowService = flowService;
        this.partService = partService;
    }

    public CodeReadingPageViewModel create(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        CodeReadingFlowState flow = flowService.getState(session, lessonId);

        if (flow.phase() == CodeReadingPhase.INTRO) {
            return base(flow.phase(), lesson, null, null, 0, false, false);
        }

        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);

        if (flow.phase() == CodeReadingPhase.SUMMARY) {
            return base(
                    flow.phase(), lesson, null, null, 0,
                    false, progress.isCompleted()
            );
        }

        LessonStep currentStep = lessonService.getStep(
                lessonId,
                progress.getCurrentStepId()
        );
        CodeReadingPart currentPart =
                partService.getPartForStep(currentStep.id());
        int completedCount = (int) currentPart.stepIds().stream()
                .filter(progress.getCompletedStepIds()::contains)
                .count();
        boolean partCompleted = completedCount == currentPart.stepIds().size();

        return new CodeReadingPageViewModel(
                flow.phase(),
                STAGE_NAME,
                LEARNING_GOAL,
                lesson.completeCode(),
                currentPart,
                currentPart.order(),
                partService.getParts().size(),
                currentStep,
                currentPart.stepIds(),
                completedCount,
                partCompleted,
                partService.isLastPart(currentPart),
                currentPart.completionNotes(),
                List.of(),
                false
        );
    }

    private CodeReadingPageViewModel base(
            CodeReadingPhase phase,
            Lesson lesson,
            CodeReadingPart currentPart,
            LessonStep currentStep,
            int completedCount,
            boolean partCompleted,
            boolean runEnabled
    ) {
        return new CodeReadingPageViewModel(
                phase,
                STAGE_NAME,
                LEARNING_GOAL,
                lesson.completeCode(),
                currentPart,
                0,
                partService.getParts().size(),
                currentStep,
                List.of(),
                completedCount,
                partCompleted,
                false,
                List.of(),
                phase == CodeReadingPhase.SUMMARY
                        ? partService.getParts()
                        : List.of(),
                runEnabled
        );
    }
}
