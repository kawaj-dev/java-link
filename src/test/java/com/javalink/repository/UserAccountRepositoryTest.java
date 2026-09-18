package com.javalink.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.javalink.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void ユーザーを保存するとIDが自動採番される() {
        UserAccount saved = userAccountRepository.saveAndFlush(
                userAccount("Java Learner", "learner@example.com"));

        assertNotNull(saved.getId());
    }

    @Test
    void ユーザーを保存すると作成日時が設定される() {
        UserAccount saved = userAccountRepository.saveAndFlush(
                userAccount("Java Learner", "learner@example.com"));

        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void 保存したユーザーをメールアドレスで取得できる() {
        userAccountRepository.saveAndFlush(
                userAccount("Java Learner", "learner@example.com"));

        UserAccount found = userAccountRepository
                .findByEmail("learner@example.com")
                .orElseThrow();

        assertEquals("Java Learner", found.getDisplayName());
        assertEquals("learner@example.com", found.getEmail());
        assertEquals("stored-password-hash", found.getPasswordHash());
    }

    @Test
    void メールアドレスが登録済みか確認できる() {
        userAccountRepository.saveAndFlush(
                userAccount("Java Learner", "learner@example.com"));

        assertTrue(userAccountRepository.existsByEmail("learner@example.com"));
        assertFalse(userAccountRepository.existsByEmail("unknown@example.com"));
    }

    @Test
    void 同じメールアドレスを二件保存すると制約違反になる() {
        userAccountRepository.saveAndFlush(
                userAccount("First Learner", "learner@example.com"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userAccountRepository.saveAndFlush(
                        userAccount("Second Learner", "learner@example.com")));
    }

    private UserAccount userAccount(String displayName, String email) {
        UserAccount userAccount = new UserAccount();
        userAccount.setDisplayName(displayName);
        userAccount.setEmail(email);
        userAccount.setPasswordHash("stored-password-hash");
        return userAccount;
    }
}
