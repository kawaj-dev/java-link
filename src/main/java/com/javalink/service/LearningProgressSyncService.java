package com.javalink.service;

import com.javalink.model.LessonProgress;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class LearningProgressSyncService {

    private final AuthenticatedUserService authenticatedUserService;
    private final LearningProgressService learningProgressService;

    public LearningProgressSyncService(
            AuthenticatedUserService authenticatedUserService,
            LearningProgressService learningProgressService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.learningProgressService = learningProgressService;
    }

    public void saveIfAuthenticated(
            HttpSession session,
            LessonProgress progress
    ) {
        authenticatedUserService.findAuthenticatedUser(session)
                .ifPresent(userAccount -> learningProgressService.saveProgress(
                        userAccount,
                        progress.getLessonId(),
                        progress.getCurrentStepId(),
                        progress.isCompleted()
                ));
    }
}
