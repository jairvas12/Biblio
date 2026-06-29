package com.library.notification_service.dto;

import com.library.notification_service.model.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificationResponseDTO(

        Long id,
        Long userId,
        String title,
        String message,
        TipoNotificacion type,
        boolean read,
        LocalDateTime createdAt
) {
}