package com.javalink.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.LearningStepProgress;
import com.javalink.entity.UserAccount;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class LearningStepProgressRepositoryTest {

    @Autowired
    private LearningStepProgressRepository learningStepProgressRepository;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 完了済みStepを親進捗へ関連付けて保存し取得できる() {
        LearningProgress progress = saveLearningProgress(
                "learner@example.com",
                "stage1"
        );

        LearningStepProgress saved = learningStepProgressRepository.saveAndFlush(
                stepProgress(progress, "public")
        );
        entityManager.clear();

        List<LearningStepProgress> found = learningStepProgressRepository
                .findAllByLearningProgress(progress);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCompletedAt());
        assertEquals(1, found.size());
        assertEquals("public", found.get(0).getStepId());
        assertEquals(progress.getId(), found.get(0).getLearningProgress().getId());
    }

    @Test
    void 同じ親進捗と同じStepは二件保存できない() {
        LearningProgress progress = saveLearningProgress(
                "learner@example.com",
                "stage1"
        );
        learningStepProgressRepository.saveAndFlush(
                stepProgress(progress, "public")
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> learningStepProgressRepository.saveAndFlush(
                        stepProgress(progress, "public")
                )
        );
    }

    @Test
    void 同じ親進捗でも別のStepならそれぞれ保存できる() {
        LearningProgress progress = saveLearningProgress(
                "learner@example.com",
                "stage1"
        );

        learningStepProgressRepository.saveAndFlush(
                stepProgress(progress, "public")
        );
        learningStepProgressRepository.saveAndFlush(
                stepProgress(progress, "class")
        );

        assertEquals(2, learningStepProgressRepository.count());
    }

    @Test
    void 別の親進捗なら同じStepをそれぞれ保存できる() {
        LearningProgress firstProgress = saveLearningProgress(
                "first@example.com",
                "stage1"
        );
        LearningProgress secondProgress = saveLearningProgress(
                "second@example.com",
                "stage1"
        );

        learningStepProgressRepository.saveAndFlush(
                stepProgress(firstProgress, "public")
        );
        learningStepProgressRepository.saveAndFlush(
                stepProgress(secondProgress, "public")
        );

        assertEquals(2, learningStepProgressRepository.count());
    }

    private LearningProgress saveLearningProgress(
            String email,
            String lessonId
    ) {
        UserAccount userAccount = new UserAccount();
        userAccount.setDisplayName("Java Learner");
        userAccount.setEmail(email);
        userAccount.setPasswordHash("stored-password-hash");
        userAccount = userAccountRepository.saveAndFlush(userAccount);

        LearningProgress progress = new LearningProgress();
        progress.setUserAccount(userAccount);
        progress.setLessonId(lessonId);
        progress.setCurrentStepId("public");
        progress.setCompleted(false);
        return learningProgressRepository.saveAndFlush(progress);
    }

    private LearningStepProgress stepProgress(
            LearningProgress learningProgress,
            String stepId
    ) {
        LearningStepProgress stepProgress = new LearningStepProgress();
        stepProgress.setLearningProgress(learningProgress);
        stepProgress.setStepId(stepId);
        return stepProgress;
    }
}
