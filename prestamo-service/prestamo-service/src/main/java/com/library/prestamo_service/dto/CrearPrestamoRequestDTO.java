package com.library.prestamo_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearPrestamoRequestDTO {

    @NotNull(message = "El identificador del usuario es obligatorio")
    @Positive(message = "El identificador del usuario debe ser mayor que cero")
    private Long usuarioId;

    @NotNull(message = "El identificador de la copia es obligatorio")
    @Positive(message = "El identificador de la copia debe ser mayor que cero")
    private Long copiaId;

    @NotNull(message = "La cantidad de días del préstamo es obligatoria")
    @Min(
            value = 1,
            message = "El préstamo debe durar al menos 1 día"
    )
    @Max(
            value = 30,
            message = "El préstamo no puede superar los 30 días"
    )
    private Integer diasPrestamo;

    @Size(
            max = 500,
            message = "La observación no puede superar los 500 caracteres"
    )
    private String observacion;
}