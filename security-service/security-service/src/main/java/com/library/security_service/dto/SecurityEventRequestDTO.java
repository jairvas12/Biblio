package com.library.security_service.dto;

import com.library.security_service.model.SecurityEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SecurityEventRequestDTO(

        @Positive(message = "El ID del usuario debe ser mayor que cero")
        Long userId,

        @Size(
                max = 150,
                message = "El nombre de usuario no puede superar los 150 caracteres"
        )
        String username,

        @NotNull(message = "El tipo de evento es obligatorio")
        SecurityEventType type,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(
                max = 1000,
                message = "La descripción no puede superar los 1000 caracteres"
        )
        String description,

        @Size(
                max = 45,
                message = "La dirección IP no puede superar los 45 caracteres"
        )
        String ipAddress,

        @NotNull(message = "El resultado del evento es obligatorio")
        Boolean successful
) {
}