package com.library.security_service.dto;

import com.library.security_service.model.SecurityEventType;

import java.time.LocalDateTime;

public record SecurityEventResponseDTO(

        Long id,
        Long userId,
        String username,
        SecurityEventType type,
        String description,
        String ipAddress,
        boolean successful,
        LocalDateTime createdAt
) {
}