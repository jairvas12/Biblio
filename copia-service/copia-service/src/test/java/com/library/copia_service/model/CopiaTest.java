
package com.library.copia_service.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CopiaTest {

    @Test
    void prePersistAsignaFechaYEstadoCuandoSonNulos() {
        Copia copia = Copia.builder()
                .codigoCopia("COPY-200")
                .bookId(2L)
                .ubicacion("Estante A-1")
                .build();

        copia.prePersist();

        assertNotNull(
                copia.getFechaRegistro()
        );

        assertEquals(
                EstadoCopia.AVAILABLE,
                copia.getEstado()
        );
    }

    @Test
    void prePersistConservaFechaYEstadoExistentes() {
        LocalDateTime fechaExistente =
                LocalDateTime.of(
                        2026,
                        6,
                        29,
                        8,
                        0
                );

        Copia copia = Copia.builder()
                .codigoCopia("COPY-201")
                .bookId(2L)
                .estado(EstadoCopia.DAMAGED)
                .ubicacion("Estante de reparación")
                .fechaRegistro(fechaExistente)
                .build();

        copia.prePersist();

        assertEquals(
                fechaExistente,
                copia.getFechaRegistro()
        );

        assertEquals(
                EstadoCopia.DAMAGED,
                copia.getEstado()
        );
    }
}
