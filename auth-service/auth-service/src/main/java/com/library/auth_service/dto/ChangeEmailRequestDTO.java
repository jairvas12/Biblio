package com.library.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeEmailRequestDTO {

    @NotBlank(message = "El nuevo correo es obligatorio")
    @Email(message = "El formato del nuevo correo no es válido")
    @Size(
            max = 150,
            message = "El nuevo correo no puede superar los 150 caracteres"
    )
    private String newEmail;
}
