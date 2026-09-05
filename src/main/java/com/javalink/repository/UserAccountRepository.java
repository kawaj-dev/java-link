package com.javalink.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javalink.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}