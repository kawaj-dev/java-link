package com.javalink.service;

import java.util.List;
import java.util.Optional;

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
                        learningProgressService.findProgress(userAccount, lessonId))
                .flatMap(progress -> restore(progress, lessonId));
    }

    private Optional<LessonProgress> restore(
            LearningProgress storedProgress,
            String lessonId
    ) {
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
