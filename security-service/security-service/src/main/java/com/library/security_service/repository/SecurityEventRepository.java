package com.library.security_service.repository;

import com.library.security_service.model.SecurityEvent;
import com.library.security_service.model.SecurityEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findAllByOrderByCreatedAtDesc();

    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<SecurityEvent> findByTypeOrderByCreatedAtDesc(
            SecurityEventType type
    );
}