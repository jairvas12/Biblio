package com.library.prestamo_service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DevolverPrestamoRequestDTO {

    @Size(
            max = 500,
            message = "La observación de devolución no puede superar los 500 caracteres"
    )
    private String observacion;
}