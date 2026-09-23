package com.javalink.service;

import java.util.Optional;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    public static final String AUTHENTICATED_USER_ID = "AUTHENTICATED_USER_ID";

    private final UserAccountRepository userAccountRepository;

    public AuthenticatedUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public void storeAuthenticatedUser(
            HttpSession session,
            UserAccount userAccount
    ) {
        session.setAttribute(AUTHENTICATED_USER_ID, userAccount.getId());
    }

    public Optional<UserAccount> findAuthenticatedUser(HttpSession session) {
        Object userId = session.getAttribute(AUTHENTICATED_USER_ID);
        if (!(userId instanceof Long authenticatedUserId)) {
            return Optional.empty();
        }

        return userAccountRepository.findById(authenticatedUserId);
    }
}
