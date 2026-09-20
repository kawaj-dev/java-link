package com.javalink.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;

public interface LearningProgressRepository
        extends JpaRepository<LearningProgress, Long> {

    Optional<LearningProgress> findByUserAccountAndLessonId(
            UserAccount userAccount,
            String lessonId
    );
}
