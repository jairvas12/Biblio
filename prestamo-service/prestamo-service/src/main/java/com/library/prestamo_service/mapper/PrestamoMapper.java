package com.library.prestamo_service.mapper;

import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.model.Prestamo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PrestamoMapper {

    /**
     * Convierte una entidad Prestamo en el DTO que será
     * entregado como respuesta por la API.
     */
    public PrestamoResponseDTO convertirAResponseDTO(
            Prestamo prestamo
    ) {
        return PrestamoResponseDTO.builder()
                .id(prestamo.getId())
                .usuarioId(prestamo.getUsuarioId())
                .copiaId(prestamo.getCopiaId())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaVencimiento(prestamo.getFechaVencimiento())
                .fechaDevolucion(prestamo.getFechaDevolucion())
                .estado(prestamo.getEstado())
                .observacion(prestamo.getObservacion())
                .diasAtraso(calcularDiasAtraso(prestamo))
                .creadoEn(prestamo.getCreadoEn())
                .actualizadoEn(prestamo.getActualizadoEn())
                .build();
    }

    /**
     * Calcula los días de atraso sin guardar el resultado
     * en la base de datos, porque cambia con el paso del tiempo.
     */
    public long calcularDiasAtraso(
            Prestamo prestamo
    ) {
        if (prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
            return 0L;
        }

        LocalDate fechaVencimiento =
                prestamo.getFechaVencimiento();

        LocalDate fechaComparacion;

        if (prestamo.getFechaDevolucion() != null) {
            fechaComparacion = prestamo.getFechaDevolucion();
        } else {
            fechaComparacion = LocalDate.now();
        }

        if (!fechaComparacion.isAfter(fechaVencimiento)) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(
                fechaVencimiento,
                fechaComparacion
        );
    }
}