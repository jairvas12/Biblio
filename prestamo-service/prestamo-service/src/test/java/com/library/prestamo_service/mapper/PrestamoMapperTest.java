package com.library.prestamo_service.mapper;

import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.model.Prestamo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrestamoMapperTest {

    private final PrestamoMapper prestamoMapper =
            new PrestamoMapper();

    @Test
    void convertirAResponseDTO_deberiaMapearTodosLosCampos() {

        LocalDate fechaPrestamo =
                LocalDate.now();

        LocalDate fechaVencimiento =
                fechaPrestamo.plusDays(7);

        LocalDateTime creadoEn =
                LocalDateTime.now().minusHours(1);

        LocalDateTime actualizadoEn =
                LocalDateTime.now();

        Prestamo prestamo =
                Prestamo.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaPrestamo(fechaPrestamo)
                        .fechaVencimiento(fechaVencimiento)
                        .fechaDevolucion(null)
                        .estado(EstadoPrestamo.ACTIVO)
                        .observacion("Préstamo de prueba")
                        .creadoEn(creadoEn)
                        .actualizadoEn(actualizadoEn)
                        .build();

        PrestamoResponseDTO resultado =
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                );

        assertAll(
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        10L,
                        resultado.getUsuarioId()
                ),
                () -> assertEquals(
                        20L,
                        resultado.getCopiaId()
                ),
                () -> assertEquals(
                        fechaPrestamo,
                        resultado.getFechaPrestamo()
                ),
                () -> assertEquals(
                        fechaVencimiento,
                        resultado.getFechaVencimiento()
                ),
                () -> assertNull(
                        resultado.getFechaDevolucion()
                ),
                () -> assertEquals(
                        EstadoPrestamo.ACTIVO,
                        resultado.getEstado()
                ),
                () -> assertEquals(
                        "Préstamo de prueba",
                        resultado.getObservacion()
                ),
                () -> assertEquals(
                        0L,
                        resultado.getDiasAtraso()
                ),
                () -> assertEquals(
                        creadoEn,
                        resultado.getCreadoEn()
                ),
                () -> assertEquals(
                        actualizadoEn,
                        resultado.getActualizadoEn()
                )
        );
    }

    @Test
    void calcularDiasAtraso_deberiaRetornarCeroCuandoPrestamoEstaCancelado() {

        Prestamo prestamo =
                Prestamo.builder()
                        .fechaVencimiento(
                                LocalDate.now().minusDays(10)
                        )
                        .estado(EstadoPrestamo.CANCELADO)
                        .build();

        long resultado =
                prestamoMapper.calcularDiasAtraso(
                        prestamo
                );

        assertEquals(
                0L,
                resultado
        );
    }

    @Test
    void calcularDiasAtraso_deberiaCalcularDiasCuandoFueDevueltoTarde() {

        LocalDate fechaVencimiento =
                LocalDate.of(
                        2026,
                        6,
                        10
                );

        Prestamo prestamo =
                Prestamo.builder()
                        .fechaVencimiento(fechaVencimiento)
                        .fechaDevolucion(
                                fechaVencimiento.plusDays(4)
                        )
                        .estado(EstadoPrestamo.DEVUELTO)
                        .build();

        long resultado =
                prestamoMapper.calcularDiasAtraso(
                        prestamo
                );

        assertEquals(
                4L,
                resultado
        );
    }
}
