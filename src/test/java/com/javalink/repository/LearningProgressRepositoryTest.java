package com.javalink.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class LearningProgressRepositoryTest {

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ユーザーに関連付けた進捗を保存して検索できる() {
        UserAccount userAccount = saveUserAccount(
                "Java Learner",
                "learner@example.com"
        );

        LearningProgress saved = learningProgressRepository.saveAndFlush(
                learningProgress(userAccount, "stage1", "public", false)
        );

        assertNotNull(saved.getId());
        assertNotNull(saved.getUpdatedAt());

        LearningProgress found = learningProgressRepository
                .findByUserAccountAndLessonId(userAccount, "stage1")
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals(userAccount.getId(), found.getUserAccount().getId());
        assertEquals("stage1", found.getLessonId());
        assertEquals("public", found.getCurrentStepId());
        assertFalse(found.isCompleted());
    }

    @Test
    void 同じユーザーと同じ教材の進捗は二件保存できない() {
        UserAccount userAccount = saveUserAccount(
                "Java Learner",
                "learner@example.com"
        );
        learningProgressRepository.saveAndFlush(
                learningProgress(userAccount, "stage1", "public", false)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> learningProgressRepository.saveAndFlush(
                        learningProgress(
                                userAccount,
                                "stage1",
                                "class",
                                false
                        )
                )
        );
    }

    @Test
    void 同じユーザーでも別の教材ならそれぞれ保存できる() {
        UserAccount userAccount = saveUserAccount(
                "Java Learner",
                "learner@example.com"
        );

        learningProgressRepository.saveAndFlush(
                learningProgress(userAccount, "stage1", "public", false)
        );
        learningProgressRepository.saveAndFlush(
                learningProgress(userAccount, "stage2", "int", false)
        );

        assertEquals(2, learningProgressRepository.count());
    }

    @Test
    void 別のユーザーなら同じ教材の進捗をそれぞれ保存できる() {
        UserAccount firstUser = saveUserAccount(
                "First Learner",
                "first@example.com"
        );
        UserAccount secondUser = saveUserAccount(
                "Second Learner",
                "second@example.com"
        );

        learningProgressRepository.saveAndFlush(
                learningProgress(firstUser, "stage1", "public", false)
        );
        learningProgressRepository.saveAndFlush(
                learningProgress(secondUser, "stage1", "public", false)
        );

        assertEquals(2, learningProgressRepository.count());
    }

    @Test
    void 既存の進捗を再保存すると新規作成せず内容を更新する() {
        UserAccount userAccount = saveUserAccount(
                "Java Learner",
                "learner@example.com"
        );
        LearningProgress saved = learningProgressRepository.saveAndFlush(
                learningProgress(userAccount, "stage1", "public", false)
        );
        Long progressId = saved.getId();

        saved.setCurrentStepId("main");
        saved.setCompleted(true);
        learningProgressRepository.saveAndFlush(saved);
        entityManager.clear();

        LearningProgress updated = learningProgressRepository
                .findById(progressId)
                .orElseThrow();

        assertEquals(1, learningProgressRepository.count());
        assertEquals("main", updated.getCurrentStepId());
        assertTrue(updated.isCompleted());
        assertNotNull(updated.getUpdatedAt());
    }

    private UserAccount saveUserAccount(String displayName, String email) {
        UserAccount userAccount = new UserAccount();
        userAccount.setDisplayName(displayName);
        userAccount.setEmail(email);
        userAccount.setPasswordHash("stored-password-hash");
        return userAccountRepository.saveAndFlush(userAccount);
    }

    private LearningProgress learningProgress(
            UserAccount userAccount,
            String lessonId,
            String currentStepId,
            boolean completed
    ) {
        LearningProgress progress = new LearningProgress();
        progress.setUserAccount(userAccount);
        progress.setLessonId(lessonId);
        progress.setCurrentStepId(currentStepId);
        progress.setCompleted(completed);
        return progress;
    }
}
