package com.library.prestamo_service.dto;

import com.library.prestamo_service.model.EstadoPrestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoResponseDTO {

    private Long id;

    private Long usuarioId;

    private Long copiaId;

    private LocalDate fechaPrestamo;

    private LocalDate fechaVencimiento;

    private LocalDate fechaDevolucion;

    private EstadoPrestamo estado;

    private String observacion;

    private Long diasAtraso;

    private LocalDateTime creadoEn;

    private LocalDateTime actualizadoEn;
}