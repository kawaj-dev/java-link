package com.javalink.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;
import com.javalink.repository.LearningProgressRepository;

@Service
public class LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;

    public LearningProgressService(LearningProgressRepository learningProgressRepository) {
        this.learningProgressRepository = learningProgressRepository;
    }

    public LearningProgress saveProgress(
            UserAccount userAccount,
            String lessonId,
            String currentStepId,
            boolean completed
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
        return learningProgressRepository.save(progress);
    }

    public Optional<LearningProgress> findProgress(UserAccount userAccount, String lessonId) {
        return learningProgressRepository.findByUserAccountAndLessonId(userAccount, lessonId);
    }
}
