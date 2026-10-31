package com.javalink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.LearningStepProgress;

public interface LearningStepProgressRepository
        extends JpaRepository<LearningStepProgress, Long> {

    List<LearningStepProgress> findAllByLearningProgress(
            LearningProgress learningProgress
    );
}
