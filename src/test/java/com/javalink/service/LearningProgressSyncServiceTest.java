package com.javalink.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

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
        progress.completeStep("public");
        progress.completeStep("class");
        progress.setCompleted(true);
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.saveIfAuthenticated(session, progress);

        verify(authenticatedUserService).findAuthenticatedUser(session);
        verify(learningProgressService).saveProgress(
                userAccount,
                "stage1",
                "class",
                true,
                Set.of("public", "class")
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

    @Test
    void ログイン済みなら対象LessonのDB進捗を削除する() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(session, "stage1");

        verify(learningProgressService)
                .deleteProgress(userAccount, "stage1");
    }

    @Test
    void 未ログインならDB進捗を削除しない() {
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.empty());

        learningProgressSyncService.resetIfAuthenticated(session, "stage1");

        verify(authenticatedUserService).findAuthenticatedUser(session);
        verifyNoInteractions(learningProgressService);
    }

    @Test
    void Stage4リセットはStage4のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE4_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE4_LESSON_ID
        );
    }

    @Test
    void Stage5リセットはStage5のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE5_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE5_LESSON_ID
        );
    }

    @Test
    void Stage6リセットはStage6のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE6_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE6_LESSON_ID
        );
    }

    @Test
    void Stage7リセットはStage7のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE7_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE7_LESSON_ID
        );
    }

    @Test
    void Stage8リセットはStage8のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE8_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE8_LESSON_ID
        );
    }

    @Test
    void Stage9リセットはStage9のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE9_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE9_LESSON_ID
        );
    }

    @Test
    void Stage10リセットはStage10のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE10_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE10_LESSON_ID
        );
    }

    @Test
    void Stage11リセットはStage11のLessonIdだけを削除Serviceへ渡す() {
        UserAccount userAccount = new UserAccount();
        when(authenticatedUserService.findAuthenticatedUser(session))
                .thenReturn(Optional.of(userAccount));

        learningProgressSyncService.resetIfAuthenticated(
                session,
                CodeReadingLessonCatalog.STAGE11_LESSON_ID
        );

        verify(learningProgressService).deleteProgress(
                userAccount,
                CodeReadingLessonCatalog.STAGE11_LESSON_ID
        );
    }
}
