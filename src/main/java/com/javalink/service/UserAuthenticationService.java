package com.javalink.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.javalink.entity.UserAccount;
import com.javalink.repository.UserAccountRepository;

@Service
public class UserAuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthenticationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserAccount> authenticate(String email, String rawPassword) {
        return userAccountRepository.findByEmail(email)
                .filter(userAccount -> passwordEncoder.matches(
                        rawPassword,
                        userAccount.getPasswordHash()));
    }
}
