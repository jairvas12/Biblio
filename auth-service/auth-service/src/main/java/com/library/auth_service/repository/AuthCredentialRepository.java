package com.library.auth_service.repository;

import com.library.auth_service.model.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthCredentialRepository
        extends JpaRepository<AuthCredential, Long> {

    Optional<AuthCredential> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    Optional<AuthCredential> findByUserId(
            Long userId
    );
    boolean existsByEmailIgnoreCaseAndUserIdNot(
            String email,
            Long userId
    );
}