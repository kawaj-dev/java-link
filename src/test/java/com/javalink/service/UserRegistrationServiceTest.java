package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        userRegistrationService = new UserRegistrationService(
                userAccountRepository,
                passwordEncoder);
    }

    @Test
    void 未登録メールならパスワードをハッシュ化してユーザーを保存する() {
        when(userAccountRepository.existsByEmail("learner@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        UserAccount savedUserAccount = new UserAccount();
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenReturn(savedUserAccount);

        UserAccount result = userRegistrationService.register(
                "Java Learner",
                "learner@example.com",
                "password");

        ArgumentCaptor<UserAccount> userAccountCaptor =
                ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).existsByEmail("learner@example.com");
        verify(passwordEncoder).encode("password");
        verify(userAccountRepository).save(userAccountCaptor.capture());

        UserAccount userAccountToSave = userAccountCaptor.getValue();
        assertEquals("Java Learner", userAccountToSave.getDisplayName());
        assertEquals("learner@example.com", userAccountToSave.getEmail());
        assertEquals("encoded-password", userAccountToSave.getPasswordHash());
        assertNotEquals("password", userAccountToSave.getPasswordHash());
        assertSame(savedUserAccount, result);
    }

    @Test
    void 登録済みメールなら例外を送出してハッシュ化も保存もしない() {
        when(userAccountRepository.existsByEmail("learner@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userRegistrationService.register(
                        "Java Learner",
                        "learner@example.com",
                        "password"));

        verify(userAccountRepository).existsByEmail("learner@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }
}
