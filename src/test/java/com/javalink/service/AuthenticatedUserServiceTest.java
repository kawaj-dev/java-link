package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private HttpSession session;

    private AuthenticatedUserService authenticatedUserService;

    @BeforeEach
    void setUp() {
        authenticatedUserService = new AuthenticatedUserService(userAccountRepository);
    }

    @Test
    void 認証済みユーザーのIDだけをセッションへ保存する() {
        UserAccount userAccount = mock(UserAccount.class);
        when(userAccount.getId()).thenReturn(42L);

        authenticatedUserService.storeAuthenticatedUser(session, userAccount);

        verify(userAccount).getId();
        verify(session).setAttribute(AuthenticatedUserService.AUTHENTICATED_USER_ID, 42L);
    }

    @Test
    void セッションにユーザーIDがなければ空を返してRepositoryを呼ばない() {
        when(session.getAttribute(AuthenticatedUserService.AUTHENTICATED_USER_ID))
                .thenReturn(null);

        Optional<UserAccount> result =
                authenticatedUserService.findAuthenticatedUser(session);

        assertTrue(result.isEmpty());
        verifyNoInteractions(userAccountRepository);
    }

    @Test
    void セッションのユーザーIDからUserAccountを取得する() {
        UserAccount userAccount = new UserAccount();
        when(session.getAttribute(AuthenticatedUserService.AUTHENTICATED_USER_ID))
                .thenReturn(42L);
        Optional<UserAccount> found = Optional.of(userAccount);
        when(userAccountRepository.findById(42L)).thenReturn(found);

        Optional<UserAccount> result =
                authenticatedUserService.findAuthenticatedUser(session);

        verify(userAccountRepository).findById(42L);
        assertSame(found, result);
        assertSame(userAccount, result.orElseThrow());
    }

    @Test
    void セッションにIDがあってもDBにユーザーがなければ空を返す() {
        when(session.getAttribute(AuthenticatedUserService.AUTHENTICATED_USER_ID))
                .thenReturn(42L);
        when(userAccountRepository.findById(42L)).thenReturn(Optional.empty());

        Optional<UserAccount> result =
                authenticatedUserService.findAuthenticatedUser(session);

        verify(userAccountRepository).findById(42L);
        assertEquals(Optional.empty(), result);
    }
}
