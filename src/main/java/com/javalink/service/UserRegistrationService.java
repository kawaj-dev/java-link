package com.javalink.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;

@Service
public class UserRegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(String displayName, String email, String rawPassword) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        UserAccount userAccount = new UserAccount();
        userAccount.setDisplayName(displayName);
        userAccount.setEmail(email);
        userAccount.setPasswordHash(passwordHash);

        return userAccountRepository.save(userAccount);
    }
}