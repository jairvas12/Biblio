package com.library.copia_service.controller;

import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.service.CopiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopiaControllerTest {

    @Mock
    private CopiaService copiaService;

    private CopiaController copiaController;

    @BeforeEach
    void setUp() {
        copiaController = new CopiaController(copiaService);
    }

    @Test
    void crearCopiaDevuelve201() {
        CopiaRequestDTO request = new CopiaRequestDTO(
                "COPY-100",
                2L,
                null,
                "Estante A-1",
                "Copia nueva"
        );

        CopiaResponseDTO respuestaServicio =
                crearRespuestaCopia();

        when(copiaService.crearCopia(request))
                .thenReturn(respuestaServicio);

        ResponseEntity<CopiaResponseDTO> respuesta =
                copiaController.crearCopia(request);

        assertEquals(
                HttpStatus.CREATED,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());
        assertSame(
                respuestaServicio,
                respuesta.getBody()
        );

        assertEquals(
                "COPY-100",
                respuesta.getBody().getCodigoCopia()
        );

        verify(copiaService)
                .crearCopia(request);
    }

    @Test
    void listarCopiasDevuelve200() {
        CopiaResponseDTO copia =
                crearRespuestaCopia();

        when(copiaService.listarCopias())
                .thenReturn(List.of(copia));

        ResponseEntity<List<CopiaResponseDTO>> respuesta =
                copiaController.listarCopias();

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());
        assertEquals(
                1,
                respuesta.getBody().size()
        );

        assertEquals(
                "COPY-100",
                respuesta.getBody()
                        .get(0)
                        .getCodigoCopia()
        );

        verify(copiaService)
                .listarCopias();
    }

    @Test
    void buscarCopiaPorIdDevuelve200() {
        CopiaResponseDTO copia =
                crearRespuestaCopia();

        when(copiaService.buscarCopiaPorId(10L))
                .thenReturn(copia);

        ResponseEntity<CopiaResponseDTO> respuesta =
                copiaController.buscarCopiaPorId(10L);

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());
        assertEquals(
                10L,
                respuesta.getBody().getId()
        );

        assertEquals(
                EstadoCopia.AVAILABLE,
                respuesta.getBody().getEstado()
        );

        verify(copiaService)
                .buscarCopiaPorId(10L);
    }

    private CopiaResponseDTO crearRespuestaCopia() {
        return CopiaResponseDTO.builder()
                .id(10L)
                .codigoCopia("COPY-100")
                .bookId(2L)
                .bookTitle("Historia de Chile")
                .bookAuthor("Francisco Antonio Encina")
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-1")
                .observacion("Copia nueva")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                7,
                                0
                        )
                )
                .build();
    }

@Test
void listarCopiasPorLibroDevuelve200() {
    CopiaResponseDTO copia =
            crearRespuestaCopia();

    when(copiaService.listarCopiasPorLibro(2L))
            .thenReturn(List.of(copia));

    ResponseEntity<List<CopiaResponseDTO>> respuesta =
            copiaController.listarCopiasPorLibro(2L);

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());
    assertEquals(
            1,
            respuesta.getBody().size()
    );

    assertEquals(
            2L,
            respuesta.getBody()
                    .get(0)
                    .getBookId()
    );

    verify(copiaService)
            .listarCopiasPorLibro(2L);
}

@Test
void listarCopiasDisponiblesPorLibroDevuelve200() {
    CopiaResponseDTO copia =
            crearRespuestaCopia();

    when(copiaService.listarCopiasDisponiblesPorLibro(2L))
            .thenReturn(List.of(copia));

    ResponseEntity<List<CopiaResponseDTO>> respuesta =
            copiaController.listarCopiasDisponiblesPorLibro(2L);

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());
    assertEquals(
            1,
            respuesta.getBody().size()
    );

    assertEquals(
            EstadoCopia.AVAILABLE,
            respuesta.getBody()
                    .get(0)
                    .getEstado()
    );

    verify(copiaService)
            .listarCopiasDisponiblesPorLibro(2L);
}

@Test
void contarCopiasDisponiblesPorLibroDevuelve200() {
    when(copiaService.contarCopiasDisponiblesPorLibro(2L))
            .thenReturn(3L);

    ResponseEntity<Long> respuesta =
            copiaController.contarCopiasDisponiblesPorLibro(2L);

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());
    assertEquals(
            3L,
            respuesta.getBody()
    );

    verify(copiaService)
            .contarCopiasDisponiblesPorLibro(2L);
}

@Test
void listarCopiasPorEstadoDevuelve200() {
    CopiaResponseDTO copia =
            crearRespuestaCopia();

    when(copiaService.listarCopiasPorEstado(
            EstadoCopia.AVAILABLE
    )).thenReturn(List.of(copia));

    ResponseEntity<List<CopiaResponseDTO>> respuesta =
            copiaController.listarCopiasPorEstado(
                    EstadoCopia.AVAILABLE
            );

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());

    assertEquals(
            1,
            respuesta.getBody().size()
    );

    assertEquals(
            EstadoCopia.AVAILABLE,
            respuesta.getBody()
                    .get(0)
                    .getEstado()
    );

    verify(copiaService)
            .listarCopiasPorEstado(
                    EstadoCopia.AVAILABLE
            );
}

@Test
void actualizarCopiaDevuelve200() {
    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-100",
            2L,
            null,
            "Estante B-2",
            "Copia actualizada"
    );

    CopiaResponseDTO copiaActualizada =
            crearRespuestaCopia();

    copiaActualizada.setUbicacion("Estante B-2");
    copiaActualizada.setObservacion("Copia actualizada");

    when(copiaService.actualizarCopia(
            10L,
            request
    )).thenReturn(copiaActualizada);

    ResponseEntity<CopiaResponseDTO> respuesta =
            copiaController.actualizarCopia(
                    10L,
                    request
            );

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());

    assertEquals(
            "Estante B-2",
            respuesta.getBody().getUbicacion()
    );

    assertEquals(
            "Copia actualizada",
            respuesta.getBody().getObservacion()
    );

    verify(copiaService)
            .actualizarCopia(
                    10L,
                    request
            );
}

@Test
void cambiarEstadoDevuelve200() {
    CopiaResponseDTO copiaPrestada =
            crearRespuestaCopia();

    copiaPrestada.setEstado(
            EstadoCopia.LOANED
    );

    when(copiaService.cambiarEstado(
            10L,
            EstadoCopia.LOANED
    )).thenReturn(copiaPrestada);

    ResponseEntity<CopiaResponseDTO> respuesta =
            copiaController.cambiarEstado(
                    10L,
                    EstadoCopia.LOANED
            );

    assertEquals(
            HttpStatus.OK,
            respuesta.getStatusCode()
    );

    assertNotNull(respuesta.getBody());

    assertEquals(
            EstadoCopia.LOANED,
            respuesta.getBody().getEstado()
    );

    assertEquals(
            10L,
            respuesta.getBody().getId()
    );

    verify(copiaService)
            .cambiarEstado(
                    10L,
                    EstadoCopia.LOANED
            );
}

@Test
void eliminarCopiaDevuelve204() {
    ResponseEntity<Void> respuesta =
            copiaController.eliminarCopia(10L);

    assertEquals(
            HttpStatus.NO_CONTENT,
            respuesta.getStatusCode()
    );

    assertEquals(
            null,
            respuesta.getBody()
    );

    verify(copiaService)
            .eliminarCopia(10L);
}



}