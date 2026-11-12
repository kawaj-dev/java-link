package com.javalink.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.javalink.entity.LearningProgress;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class LearningProgressRestoreService {

    private final AuthenticatedUserService authenticatedUserService;
    private final LearningProgressService learningProgressService;
    private final LessonService lessonService;

    public LearningProgressRestoreService(
            AuthenticatedUserService authenticatedUserService,
            LearningProgressService learningProgressService,
            LessonService lessonService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.learningProgressService = learningProgressService;
        this.lessonService = lessonService;
    }

    public Optional<LessonProgress> restoreIfAuthenticated(
            HttpSession session,
            String lessonId
    ) {
        return authenticatedUserService.findAuthenticatedUser(session)
                .flatMap(userAccount ->
                        learningProgressService.findProgressSnapshot(
                                userAccount,
                                lessonId
                        ))
                .flatMap(snapshot -> restore(snapshot, lessonId));
    }

    /** ログイン中ユーザーの指定Lesson群を、一括取得したDB進捗から復元します。 */
    public Map<String, LessonProgress> restoreAllIfAuthenticated(
            HttpSession session,
            Collection<String> lessonIds
    ) {
        return authenticatedUserService.findAuthenticatedUser(session)
                .map(userAccount -> {
                    Map<String, LearningProgressService.ProgressSnapshot> snapshots =
                            learningProgressService.findProgressSnapshots(userAccount);
                    Map<String, LessonProgress> restored = new LinkedHashMap<>();
                    lessonIds.forEach(lessonId -> Optional
                            .ofNullable(snapshots.get(lessonId))
                            .flatMap(snapshot -> restore(snapshot, lessonId))
                            .ifPresent(progress -> restored.put(lessonId, progress)));
                    return Map.copyOf(restored);
                })
                .orElseGet(Map::of);
    }

    private Optional<LessonProgress> restore(
            LearningProgressService.ProgressSnapshot snapshot,
            String lessonId
    ) {
        LearningProgress storedProgress = snapshot.progress();
        Lesson lesson = lessonService.getLesson(lessonId);
        List<LessonStep> steps = lesson.steps();
        int currentIndex = findStepIndex(
                steps,
                storedProgress.getCurrentStepId()
        );

        if (currentIndex < 0) {
            return Optional.empty();
        }

        if (storedProgress.isCompleted()
                && !isLastRequiredStep(steps, storedProgress.getCurrentStepId())) {
            return Optional.empty();
        }

        if (snapshot.detailedStepProgress()) {
            return restoreDetailedProgress(
                    snapshot,
                    lessonId,
                    steps,
                    currentIndex
            );
        }

        return restoreLegacyProgress(
                storedProgress,
                lessonId,
                steps,
                currentIndex
        );
    }

    private Optional<LessonProgress> restoreDetailedProgress(
            LearningProgressService.ProgressSnapshot snapshot,
            String lessonId,
            List<LessonStep> steps,
            int currentIndex
    ) {
        LearningProgress storedProgress = snapshot.progress();
        Set<String> storedStepIds = snapshot.completedStepIds();
        Set<String> lessonStepIds = steps.stream()
                .map(LessonStep::id)
                .collect(Collectors.toSet());

        if (!lessonStepIds.containsAll(storedStepIds)
                || !hasConsistentDetailedProgress(
                        steps,
                        currentIndex,
                        storedStepIds,
                        storedProgress.isCompleted()
                )) {
            return Optional.empty();
        }

        LessonProgress restoredProgress = new LessonProgress(
                lessonId,
                storedProgress.getCurrentStepId()
        );
        storedStepIds.forEach(restoredProgress::completeStep);
        if (restoredProgress.isStepCompleted(
                restoredProgress.getCurrentStepId()
        )) {
            restoredProgress.setAnswered(true);
            restoredProgress.setCorrect(true);
        }
        restoredProgress.setCompleted(storedProgress.isCompleted());
        return Optional.of(restoredProgress);
    }

    private Optional<LessonProgress> restoreLegacyProgress(
            LearningProgress storedProgress,
            String lessonId,
            List<LessonStep> steps,
            int currentIndex
    ) {
        LessonProgress restoredProgress = new LessonProgress(
                lessonId,
                storedProgress.getCurrentStepId()
        );

        int completionLimit = storedProgress.isCompleted()
                ? steps.size()
                : currentIndex;
        steps.stream()
                .limit(completionLimit)
                .filter(LessonStep::required)
                .forEach(step -> restoredProgress.completeStep(step.id()));
        restoredProgress.setCompleted(storedProgress.isCompleted());

        return Optional.of(restoredProgress);
    }

    private boolean hasConsistentDetailedProgress(
            List<LessonStep> steps,
            int currentIndex,
            Set<String> storedStepIds,
            boolean completed
    ) {
        Set<String> completedRequiredStepIds = steps.stream()
                .filter(LessonStep::required)
                .map(LessonStep::id)
                .filter(storedStepIds::contains)
                .collect(Collectors.toSet());
        Set<String> expectedRequiredStepIds;

        if (completed) {
            expectedRequiredStepIds = steps.stream()
                    .filter(LessonStep::required)
                    .map(LessonStep::id)
                    .collect(Collectors.toSet());
        } else {
            boolean currentStepCompleted = storedStepIds.contains(
                    steps.get(currentIndex).id()
            );
            long completionLimit = currentStepCompleted
                    ? currentIndex + 1L
                    : currentIndex;
            expectedRequiredStepIds = steps.stream()
                    .limit(completionLimit)
                    .filter(LessonStep::required)
                    .map(LessonStep::id)
                    .collect(Collectors.toSet());
        }
        return completedRequiredStepIds.equals(expectedRequiredStepIds);
    }

    private int findStepIndex(List<LessonStep> steps, String stepId) {
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).id().equals(stepId)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isLastRequiredStep(
            List<LessonStep> steps,
            String currentStepId
    ) {
        for (int index = steps.size() - 1; index >= 0; index--) {
            LessonStep step = steps.get(index);
            if (step.required()) {
                return step.id().equals(currentStepId);
            }
        }
        return false;
    }
}
