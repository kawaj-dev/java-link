package com.javalink.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.LearningStepProgress;

public interface LearningStepProgressRepository
        extends JpaRepository<LearningStepProgress, Long> {

    List<LearningStepProgress> findAllByLearningProgress(
            LearningProgress learningProgress
    );

    List<LearningStepProgress> findAllByLearningProgressIn(
            Collection<LearningProgress> learningProgresses
    );

    @Modifying
    @Query("delete from LearningStepProgress step "
            + "where step.learningProgress = :learningProgress")
    int deleteAllByLearningProgress(
            @Param("learningProgress") LearningProgress learningProgress
    );
}
