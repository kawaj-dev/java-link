package com.javalink.service;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 画面フェーズと学習進捗から、Quiz画面専用ViewModelを作ります。
 */
@Service
public class CodeReadingPageViewModelService {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final CodeReadingFlowService flowService;
    private final CodeReadingPartService partService;
    private final CodeReadingLessonCatalog lessonCatalog;
    private final CodeReadingService codeReadingService;

    public CodeReadingPageViewModelService(
            LessonService lessonService,
            LessonProgressService lessonProgressService,
            CodeReadingFlowService flowService,
            CodeReadingPartService partService,
            CodeReadingLessonCatalog lessonCatalog,
            CodeReadingService codeReadingService
    ) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
        this.flowService = flowService;
        this.partService = partService;
        this.lessonCatalog = lessonCatalog;
        this.codeReadingService = codeReadingService;
    }

    public CodeReadingPageViewModel create(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        CodeReadingLessonDefinition definition =
                lessonCatalog.getDefinition(lessonId);
        CodeReadingFlowState flow = flowService.getState(session, lessonId);

        if (flow.phase() == CodeReadingPhase.INTRO) {
            return base(
                    flow.phase(), lesson, definition,
                    null, null, 0, false, false
            );
        }

        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);

        if (!isCompatibleWithCurrentLesson(progress, lesson)) {
            lessonProgressService.resetProgress(session, lessonId);
            flowService.reset(session, lessonId);
            return base(
                    CodeReadingPhase.INTRO,
                    lesson,
                    definition,
                    null,
                    null,
                    0,
                    false,
                    false
            );
        }

        if (flow.phase() == CodeReadingPhase.SUMMARY) {
            return base(
                    flow.phase(), lesson, definition, null, null, 0,
                    false, progress.isCompleted()
            );
        }

        LessonStep currentStep = lessonService.getStep(
                lessonId,
                progress.getCurrentStepId()
        );
        CodeReadingPart currentPart =
                partService.getPartForStep(lessonId, currentStep.id());
        int completedCount = (int) currentPart.stepIds().stream()
                .filter(progress.getCompletedStepIds()::contains)
                .count();
        boolean partCompleted = completedCount == currentPart.stepIds().size();

        return new CodeReadingPageViewModel(
                flow.phase(),
                definition.stageName(),
                definition.learningGoal(),
                lesson.completeCode(),
                currentPart,
                currentPart.order(),
                partService.getParts(lessonId).size(),
                currentStep,
                currentPart.stepIds(),
                completedCount,
                partCompleted,
                partService.isLastPart(lessonId, currentPart),
                currentPart.completionNotes(),
                partService.createCircuitGroups(lessonId, progress),
                codeReadingService.createCodeLines(
                        lessonId,
                        progress,
                        currentPart
                ),
                List.of(),
                false
        );
    }

    private CodeReadingPageViewModel base(
            CodeReadingPhase phase,
            Lesson lesson,
            CodeReadingLessonDefinition definition,
            CodeReadingPart currentPart,
            LessonStep currentStep,
            int completedCount,
            boolean partCompleted,
            boolean runEnabled
    ) {
        return new CodeReadingPageViewModel(
                phase,
                definition.stageName(),
                definition.learningGoal(),
                lesson.completeCode(),
                currentPart,
                0,
                partService.getParts(lesson.id()).size(),
                currentStep,
                List.of(),
                completedCount,
                partCompleted,
                false,
                List.of(),
                List.of(),
                List.of(),
                phase == CodeReadingPhase.SUMMARY
                        ? partService.getParts(lesson.id())
                        : List.of(),
                runEnabled
        );
    }

    /** 開発中にstep構成が変わった旧セッションを安全に導入へ戻します。 */
    private boolean isCompatibleWithCurrentLesson(
            LessonProgress progress,
            Lesson lesson
    ) {
        Set<String> validStepIds = lesson.steps().stream()
                .map(LessonStep::id)
                .collect(Collectors.toSet());
        return validStepIds.contains(progress.getCurrentStepId())
                && validStepIds.containsAll(progress.getCompletedStepIds());
    }
}
