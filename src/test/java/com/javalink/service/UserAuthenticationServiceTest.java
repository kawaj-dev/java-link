package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAuthenticationService userAuthenticationService;

    @BeforeEach
    void setUp() {
        userAuthenticationService = new UserAuthenticationService(
                userAccountRepository,
                passwordEncoder);
    }

    @Test
    void 登録済みメールと正しいパスワードなら認証に成功する() {
        UserAccount userAccount = userAccount("stored-hash");
        when(userAccountRepository.findByEmail("learner@example.com"))
                .thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("password", "stored-hash"))
                .thenReturn(true);

        Optional<UserAccount> authenticated = userAuthenticationService.authenticate(
                "learner@example.com",
                "password");

        assertSame(userAccount, authenticated.orElseThrow());
        verify(passwordEncoder).matches("password", "stored-hash");
        verify(passwordEncoder, never()).encode(any());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void 登録済みメールでもパスワードが違えば認証に失敗する() {
        UserAccount userAccount = userAccount("stored-hash");
        when(userAccountRepository.findByEmail("learner@example.com"))
                .thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("incorrect", "stored-hash"))
                .thenReturn(false);

        Optional<UserAccount> authenticated = userAuthenticationService.authenticate(
                "learner@example.com",
                "incorrect");

        assertTrue(authenticated.isEmpty());
        verify(passwordEncoder).matches("incorrect", "stored-hash");
        verify(passwordEncoder, never()).encode(any());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void 未登録メールならパスワードを照合せず認証に失敗する() {
        when(userAccountRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        Optional<UserAccount> authenticated = userAuthenticationService.authenticate(
                "unknown@example.com",
                "password");

        assertTrue(authenticated.isEmpty());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userAccountRepository, never()).save(any());
    }

    private UserAccount userAccount(String passwordHash) {
        UserAccount userAccount = new UserAccount();
        userAccount.setPasswordHash(passwordHash);
        return userAccount;
    }
}
