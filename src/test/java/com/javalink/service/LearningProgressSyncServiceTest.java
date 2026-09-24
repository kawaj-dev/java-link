package com.javalink.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.javalink.entity.UserAccount;
import com.javalink.model.LessonProgress;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LearningProgressSyncServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private LearningProgressService learningProgressService;

    @Mock
    private HttpSession session;

    private LearningProgressSyncService learningProgressSyncService;

    @BeforeEach
    void setUp() {
        learningProgressSyncService = new LearningProgressSyncService(
                authenticatedUserService,
                learningProgressService
        );
    }

    @Test
    void ログイン済みならセッション進捗をDB進捗保存Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        LessonProgress progress = new LessonProgress("stage1", "class");
        progress.setCompleted(true);
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.saveIfAuthenticated(session, progress);

        verify(authenticatedUserService).findAuthenticatedUser(session);
        verify(learningProgressService).saveProgress(
                userAccount,
                "stage1",
                "class",
                true
        );
    }

    @Test
    void 未ログインならDB進捗を保存しない() {
        LessonProgress progress = new LessonProgress("stage1", "class");
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.empty());

        learningProgressSyncService.saveIfAuthenticated(session, progress);

        verify(authenticatedUserService).findAuthenticatedUser(session);
        verifyNoInteractions(learningProgressService);
    }
}
