package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.LearningStepProgress;
import com.javalink.entity.UserAccount;
import com.javalink.repository.LearningProgressRepository;
import com.javalink.repository.LearningStepProgressRepository;
import com.javalink.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:learning-progress-persistence",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LearningProgressPersistenceIntegrationTest {

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Autowired
    private LearningStepProgressRepository learningStepProgressRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void setUp() {
        learningStepProgressRepository.deleteAll();
        learningProgressRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void 親進捗と複数の完了済みStepを整合した状態で保存できる() {
        UserAccount userAccount = saveUser("learner@example.com");

        LearningProgress saved = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "main",
                false,
                Set.of("public", "class")
        );

        List<LearningStepProgress> steps = learningStepProgressRepository
                .findAllByLearningProgress(saved);
        assertEquals(2, steps.size());
        assertEquals(
                Set.of("public", "class"),
                steps.stream()
                        .map(LearningStepProgress::getStepId)
                        .collect(java.util.stream.Collectors.toSet())
        );
        assertTrue(steps.stream().allMatch(step ->
                step.getLearningProgress().getId().equals(saved.getId())));
        assertTrue(steps.stream().allMatch(step ->
                step.getCompletedAt() != null));
    }

    @Test
    void 同じStepを再保存しても重複せず完了日時を保持する() {
        UserAccount userAccount = saveUser("learner@example.com");
        LearningProgress firstSave = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "class",
                false,
                Set.of("public")
        );
        LearningStepProgress storedStep = learningStepProgressRepository
                .findAllByLearningProgress(firstSave)
                .get(0);
        LocalDateTime firstCompletedAt = storedStep.getCompletedAt();

        LearningProgress secondSave = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "main",
                false,
                Set.of("public", "class")
        );

        List<LearningStepProgress> steps = learningStepProgressRepository
                .findAllByLearningProgress(secondSave);
        assertEquals(2, steps.size());
        LearningStepProgress reloadedPublic = steps.stream()
                .filter(step -> step.getStepId().equals("public"))
                .findFirst()
                .orElseThrow();
        assertEquals(storedStep.getId(), reloadedPublic.getId());
        assertEquals(firstCompletedAt, reloadedPublic.getCompletedAt());
    }

    @Test
    void Step詳細がない既存の親進捗にも完了済みStepを追加できる() {
        UserAccount userAccount = saveUser("learner@example.com");
        LearningProgress existing = new LearningProgress();
        existing.setUserAccount(userAccount);
        existing.setLessonId("stage1");
        existing.setCurrentStepId("public");
        existing.setCompleted(false);
        existing = learningProgressRepository.saveAndFlush(existing);
        assertTrue(learningStepProgressRepository
                .findAllByLearningProgress(existing)
                .isEmpty());

        LearningProgress updated = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "class",
                false,
                Set.of("public")
        );

        assertEquals(existing.getId(), updated.getId());
        assertEquals("class", updated.getCurrentStepId());
        assertEquals(Set.of("public"), stepIds(updated));
    }

    @Test
    void ユーザーとLessonごとにStep進捗を分離して保存する() {
        UserAccount firstUser = saveUser("first@example.com");
        UserAccount secondUser = saveUser("second@example.com");

        LearningProgress firstStage1 = learningProgressService.saveProgress(
                firstUser, "stage1", "class", false, Set.of("public"));
        LearningProgress firstStage2 = learningProgressService.saveProgress(
                firstUser, "stage2", "age", false, Set.of("int"));
        LearningProgress secondStage1 = learningProgressService.saveProgress(
                secondUser, "stage1", "class", false, Set.of("public"));

        assertEquals(
                Set.of("public"),
                stepIds(firstStage1)
        );
        assertEquals(Set.of("int"), stepIds(firstStage2));
        assertEquals(Set.of("public"), stepIds(secondStage1));
        assertNotEquals(firstStage1.getId(), secondStage1.getId());
    }

    @Test
    void Lesson完了時も完了済みStepを保存する() {
        UserAccount userAccount = saveUser("learner@example.com");

        LearningProgress saved = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "main",
                true,
                Set.of("public", "class", "main")
        );

        assertTrue(saved.isCompleted());
        assertEquals(Set.of("public", "class", "main"), stepIds(saved));
    }

    @Test
    void 子Step保存に失敗した場合は親進捗の更新もロールバックする() {
        UserAccount userAccount = saveUser("learner@example.com");
        LearningProgress original = learningProgressService.saveProgress(
                userAccount,
                "stage1",
                "public",
                false,
                Set.of()
        );
        Set<String> invalidStepIds = new HashSet<>();
        invalidStepIds.add(null);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> learningProgressService.saveProgress(
                        userAccount,
                        "stage1",
                        "class",
                        false,
                        invalidStepIds
                )
        );

        LearningProgress reloaded = learningProgressRepository
                .findById(original.getId())
                .orElseThrow();
        assertEquals("public", reloaded.getCurrentStepId());
        assertTrue(learningStepProgressRepository
                .findAllByLearningProgress(reloaded)
                .isEmpty());
    }

    @Test
    void 親進捗の保存に失敗した場合はStep進捗も保存しない() {
        UserAccount userAccount = saveUser("learner@example.com");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> learningProgressService.saveProgress(
                        userAccount,
                        "stage1",
                        null,
                        false,
                        Set.of("public")
                )
        );

        assertTrue(learningProgressRepository
                .findByUserAccountAndLessonId(userAccount, "stage1")
                .isEmpty());
        assertEquals(0, learningStepProgressRepository.count());
    }

    private Set<String> stepIds(LearningProgress progress) {
        return learningStepProgressRepository
                .findAllByLearningProgress(progress)
                .stream()
                .map(LearningStepProgress::getStepId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private UserAccount saveUser(String email) {
        UserAccount userAccount = new UserAccount();
        userAccount.setDisplayName("Java Learner");
        userAccount.setEmail(email);
        userAccount.setPasswordHash("stored-password-hash");
        return userAccountRepository.saveAndFlush(userAccount);
    }
}
