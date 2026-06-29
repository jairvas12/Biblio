package com.library.notification_service.dto;

import com.library.notification_service.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotificationRequestDTO(

        @NotNull(message = "El ID del usuario es obligatorio")
        @Positive(message = "El ID del usuario debe ser mayor que cero")
        Long userId,

        @NotBlank(message = "El título es obligatorio")
        @Size(
                max = 120,
                message = "El título no puede superar los 120 caracteres"
        )
        String title,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(
                max = 1000,
                message = "El mensaje no puede superar los 1000 caracteres"
        )
        String message,

        @NotNull(message = "El tipo de notificación es obligatorio")
        TipoNotificacion type
) {
}