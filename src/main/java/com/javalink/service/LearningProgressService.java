package com.javalink.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.LearningStepProgress;
import com.javalink.entity.UserAccount;
import com.javalink.repository.LearningProgressRepository;
import com.javalink.repository.LearningStepProgressRepository;

@Service
public class LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final LearningStepProgressRepository learningStepProgressRepository;

    public LearningProgressService(
            LearningProgressRepository learningProgressRepository,
            LearningStepProgressRepository learningStepProgressRepository
    ) {
        this.learningProgressRepository = learningProgressRepository;
        this.learningStepProgressRepository = learningStepProgressRepository;
    }

    @Transactional
    public LearningProgress saveProgress(
            UserAccount userAccount,
            String lessonId,
            String currentStepId,
            boolean completed,
            Set<String> completedStepIds
    ) {
        LearningProgress progress = learningProgressRepository
                .findByUserAccountAndLessonId(userAccount, lessonId)
                .orElseGet(() -> {
                    LearningProgress newProgress = new LearningProgress();
                    newProgress.setUserAccount(userAccount);
                    newProgress.setLessonId(lessonId);
                    return newProgress;
                });

        progress.setCurrentStepId(currentStepId);
        progress.setCompleted(completed);
        LearningProgress savedProgress = learningProgressRepository.save(progress);

        Set<String> storedStepIds = learningStepProgressRepository
                .findAllByLearningProgress(savedProgress)
                .stream()
                .map(LearningStepProgress::getStepId)
                .collect(Collectors.toSet());
        List<LearningStepProgress> newStepProgresses = completedStepIds.stream()
                .filter(stepId -> !storedStepIds.contains(stepId))
                .map(stepId -> createStepProgress(savedProgress, stepId))
                .toList();

        if (!newStepProgresses.isEmpty()) {
            learningStepProgressRepository.saveAll(newStepProgresses);
        }
        return savedProgress;
    }

    public Optional<LearningProgress> findProgress(UserAccount userAccount, String lessonId) {
        return learningProgressRepository.findByUserAccountAndLessonId(userAccount, lessonId);
    }

    private LearningStepProgress createStepProgress(
            LearningProgress learningProgress,
            String stepId
    ) {
        LearningStepProgress stepProgress = new LearningStepProgress();
        stepProgress.setLearningProgress(learningProgress);
        stepProgress.setStepId(stepId);
        return stepProgress;
    }
}
