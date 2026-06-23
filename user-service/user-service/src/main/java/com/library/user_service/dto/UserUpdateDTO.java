package com.library.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 2,
            max = 100,
            message = "El nombre debe contener entre 2 y 100 caracteres"
    )
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Size(
            max = 150,
            message = "El correo no puede superar los 150 caracteres"
    )
    private String email;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
            regexp = "(?i)^(ADMIN|BIBLIOTECARIO|USER)$",
            message = "El rol debe ser ADMIN, BIBLIOTECARIO o USER"
    )
    private String role;
}
