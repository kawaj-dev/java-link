package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.javalink.entity.LearningProgress;
import com.javalink.entity.UserAccount;
import com.javalink.repository.LearningProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceTest {

    @Mock
    private LearningProgressRepository learningProgressRepository;

    private LearningProgressService learningProgressService;

    @BeforeEach
    void setUp() {
        learningProgressService = new LearningProgressService(learningProgressRepository);
    }

    @Test
    void 進捗が存在しなければ新規作成して保存する() {
        UserAccount userAccount = new UserAccount();
        when(learningProgressRepository.findByUserAccountAndLessonId(userAccount, "stage1"))
                .thenReturn(Optional.empty());
        LearningProgress saved = new LearningProgress();
        when(learningProgressRepository.save(any(LearningProgress.class)))
                .thenReturn(saved);

        LearningProgress result = learningProgressService.saveProgress(
                userAccount, "stage1", "public", false);

        ArgumentCaptor<LearningProgress> captor =
                ArgumentCaptor.forClass(LearningProgress.class);
        verify(learningProgressRepository).findByUserAccountAndLessonId(userAccount, "stage1");
        verify(learningProgressRepository, times(1)).save(captor.capture());
        LearningProgress progressToSave = captor.getValue();
        assertSame(userAccount, progressToSave.getUserAccount());
        assertEquals("stage1", progressToSave.getLessonId());
        assertEquals("public", progressToSave.getCurrentStepId());
        assertEquals(false, progressToSave.isCompleted());
        assertSame(saved, result);
    }

    @Test
    void 既存進捗があれば同じEntityを更新して保存する() {
        UserAccount userAccount = new UserAccount();
        LearningProgress existing = new LearningProgress();
        existing.setUserAccount(userAccount);
        existing.setLessonId("stage1");
        existing.setCurrentStepId("public");
        existing.setCompleted(false);
        when(learningProgressRepository.findByUserAccountAndLessonId(userAccount, "stage1"))
                .thenReturn(Optional.of(existing));
        LearningProgress saved = new LearningProgress();
        when(learningProgressRepository.save(existing)).thenReturn(saved);

        LearningProgress result = learningProgressService.saveProgress(
                userAccount, "stage1", "class", true);

        verify(learningProgressRepository).findByUserAccountAndLessonId(userAccount, "stage1");
        verify(learningProgressRepository, times(1)).save(existing);
        assertSame(userAccount, existing.getUserAccount());
        assertEquals("stage1", existing.getLessonId());
        assertEquals("class", existing.getCurrentStepId());
        assertTrue(existing.isCompleted());
        assertSame(saved, result);
    }

    @Test
    void ユーザーと教材から進捗を取得する() {
        UserAccount userAccount = new UserAccount();
        Optional<LearningProgress> found = Optional.of(new LearningProgress());
        when(learningProgressRepository.findByUserAccountAndLessonId(userAccount, "stage1"))
                .thenReturn(found);

        Optional<LearningProgress> result =
                learningProgressService.findProgress(userAccount, "stage1");

        verify(learningProgressRepository).findByUserAccountAndLessonId(userAccount, "stage1");
        assertSame(found, result);
    }
}
