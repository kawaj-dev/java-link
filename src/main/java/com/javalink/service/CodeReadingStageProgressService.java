package com.javalink.service;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 学習目次に表示する、Stageごとの状態と進捗率を組み立てます。 */
@Service
public class CodeReadingStageProgressService {

    private final CodeReadingLessonCatalog lessonCatalog;
    private final LessonProgressService lessonProgressService;
    private final CodeReadingFlowService flowService;
    private final LearningProgressRestoreService restoreService;

    public CodeReadingStageProgressService(
            CodeReadingLessonCatalog lessonCatalog,
            LessonProgressService lessonProgressService,
            CodeReadingFlowService flowService,
            LearningProgressRestoreService restoreService
    ) {
        this.lessonCatalog = lessonCatalog;
        this.lessonProgressService = lessonProgressService;
        this.flowService = flowService;
        this.restoreService = restoreService;
    }

    public Map<String, StageProgress> getStageProgresses(HttpSession session) {
        List<Lesson> lessons = lessonCatalog.getLessons();
        Map<String, LessonProgress> restoredProgresses =
                restoreService.restoreAllIfAuthenticated(
                        session,
                        lessons.stream().map(Lesson::id).toList()
                );
        Map<String, StageProgress> stageProgresses = new LinkedHashMap<>();

        lessons.forEach(lesson -> stageProgresses.put(
                lesson.id(),
                resolveStageProgress(
                        lesson,
                        lessonProgressService.findStoredProgress(
                                session,
                                lesson.id()
                        ),
                        flowService.findStoredState(session, lesson.id()),
                        Optional.ofNullable(restoredProgresses.get(lesson.id()))
                )
        ));
        return Map.copyOf(stageProgresses);
    }

    private StageProgress resolveStageProgress(
            Lesson lesson,
            Optional<LessonProgress> sessionProgress,
            Optional<CodeReadingFlowState> flowState,
            Optional<LessonProgress> restoredProgress
    ) {
        if (sessionProgress.isPresent()
                && hasSessionLearningStarted(
                        lesson,
                        sessionProgress.orElseThrow(),
                        flowState
                )) {
            return toStageProgress(lesson, sessionProgress.orElseThrow(), true);
        }
        if (restoredProgress.isPresent()) {
            return toStageProgress(lesson, restoredProgress.orElseThrow(), true);
        }
        return new StageProgress(StageLearningStatus.NOT_STARTED, 0);
    }

    private boolean hasSessionLearningStarted(
            Lesson lesson,
            LessonProgress progress,
            Optional<CodeReadingFlowState> flowState
    ) {
        boolean learningPhase = flowState
                .map(CodeReadingFlowState::phase)
                .filter(phase -> phase != CodeReadingPhase.INTRO)
                .isPresent();
        String firstStepId = lesson.steps().isEmpty()
                ? ""
                : lesson.steps().get(0).id();
        return learningPhase
                || progress.isCompleted()
                || progress.isAnswered()
                || progress.isCorrect()
                || progress.isProgramExecuted()
                || !progress.getCurrentStepId().equals(firstStepId)
                || countValidCompletedSteps(lesson, progress) > 0;
    }

    private StageProgress toStageProgress(
            Lesson lesson,
            LessonProgress progress,
            boolean started
    ) {
        if (progress.isCompleted()) {
            return new StageProgress(StageLearningStatus.COMPLETED, 100);
        }
        if (!started) {
            return new StageProgress(StageLearningStatus.NOT_STARTED, 0);
        }

        int totalSteps = lesson.steps().size();
        int completedSteps = countValidCompletedSteps(lesson, progress);
        int percentage = totalSteps == 0
                ? 0
                : Math.min(100, completedSteps * 100 / totalSteps);
        return new StageProgress(StageLearningStatus.IN_PROGRESS, percentage);
    }

    private int countValidCompletedSteps(
            Lesson lesson,
            LessonProgress progress
    ) {
        Set<String> validStepIds = lesson.steps().stream()
                .map(LessonStep::id)
                .collect(Collectors.toSet());
        return (int) progress.getCompletedStepIds().stream()
                .filter(validStepIds::contains)
                .distinct()
                .count();
    }

    public record StageProgress(
            StageLearningStatus status,
            int percentage
    ) {
    }

    public enum StageLearningStatus {
        NOT_STARTED("未学習", "is-not-started"),
        IN_PROGRESS("学習中", "is-in-progress"),
        COMPLETED("✓ 学習完了", "is-completed");

        private final String label;
        private final String cssClass;

        StageLearningStatus(String label, String cssClass) {
            this.label = label;
            this.cssClass = cssClass;
        }

        public String getLabel() {
            return label;
        }

        public String getCssClass() {
            return cssClass;
        }
    }
}
