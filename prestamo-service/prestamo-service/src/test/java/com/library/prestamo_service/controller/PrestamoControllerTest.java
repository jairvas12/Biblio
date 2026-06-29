package com.library.prestamo_service.controller;

import com.library.prestamo_service.dto.ActualizacionAtrasosResponseDTO;
import com.library.prestamo_service.dto.CrearPrestamoRequestDTO;
import com.library.prestamo_service.dto.DevolverPrestamoRequestDTO;
import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.service.PrestamoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoControllerTest {

    @Mock
    private PrestamoService prestamoService;

    @InjectMocks
    private PrestamoController prestamoController;

    @Test
    void crearPrestamo_deberiaRetornarCreatedYPrestamoCreado() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Préstamo de prueba"
                );

        PrestamoResponseDTO respuestaEsperada =
                crearPrestamoResponse(
                        1L,
                        10L,
                        20L,
                        EstadoPrestamo.ACTIVO
                );

        when(
                prestamoService.crearPrestamo(requestDTO)
        ).thenReturn(
                respuestaEsperada
        );

        ResponseEntity<PrestamoResponseDTO> respuesta =
                prestamoController.crearPrestamo(
                        requestDTO
                );

        assertEquals(
                HttpStatus.CREATED,
                respuesta.getStatusCode()
        );

        assertSame(
                respuestaEsperada,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).crearPrestamo(
                requestDTO
        );
    }

    @Test
    void listarTodos_deberiaRetornarOkYTodosLosPrestamos() {

        PrestamoResponseDTO prestamoUno =
                crearPrestamoResponse(
                        1L,
                        10L,
                        20L,
                        EstadoPrestamo.ACTIVO
                );

        PrestamoResponseDTO prestamoDos =
                crearPrestamoResponse(
                        2L,
                        11L,
                        21L,
                        EstadoPrestamo.DEVUELTO
                );

        List<PrestamoResponseDTO> prestamos =
                List.of(
                        prestamoUno,
                        prestamoDos
                );

        when(
                prestamoService.listarTodos()
        ).thenReturn(
                prestamos
        );

        ResponseEntity<List<PrestamoResponseDTO>> respuesta =
                prestamoController.listarTodos();

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                prestamos,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).listarTodos();
    }

    @Test
    void obtenerPorId_deberiaRetornarOkYPrestamoEncontrado() {

        PrestamoResponseDTO respuestaEsperada =
                crearPrestamoResponse(
                        3L,
                        12L,
                        22L,
                        EstadoPrestamo.ACTIVO
                );

        when(
                prestamoService.obtenerPorId(3L)
        ).thenReturn(
                respuestaEsperada
        );

        ResponseEntity<PrestamoResponseDTO> respuesta =
                prestamoController.obtenerPorId(
                        3L
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                respuestaEsperada,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).obtenerPorId(
                3L
        );
    }

    @Test
    void listarPorUsuario_deberiaRetornarOkYPrestamosDelUsuario() {

        PrestamoResponseDTO prestamo =
                crearPrestamoResponse(
                        4L,
                        15L,
                        23L,
                        EstadoPrestamo.ATRASADO
                );

        List<PrestamoResponseDTO> prestamos =
                List.of(prestamo);

        when(
                prestamoService.listarPorUsuario(15L)
        ).thenReturn(
                prestamos
        );

        ResponseEntity<List<PrestamoResponseDTO>> respuesta =
                prestamoController.listarPorUsuario(
                        15L
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                prestamos,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).listarPorUsuario(
                15L
        );
    }


    @Test
    void listarPorCopia_deberiaRetornarOkYPrestamosDeLaCopia() {

        PrestamoResponseDTO prestamo =
                crearPrestamoResponse(
                        5L,
                        16L,
                        30L,
                        EstadoPrestamo.ACTIVO
                );

        List<PrestamoResponseDTO> prestamos =
                List.of(prestamo);

        when(
                prestamoService.listarPorCopia(30L)
        ).thenReturn(
                prestamos
        );

        ResponseEntity<List<PrestamoResponseDTO>> respuesta =
                prestamoController.listarPorCopia(
                        30L
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                prestamos,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).listarPorCopia(
                30L
        );
    }

    @Test
    void listarPorEstado_deberiaRetornarOkYPrestamosFiltrados() {

        PrestamoResponseDTO prestamo =
                crearPrestamoResponse(
                        6L,
                        17L,
                        31L,
                        EstadoPrestamo.ATRASADO
                );

        List<PrestamoResponseDTO> prestamos =
                List.of(prestamo);

        when(
                prestamoService.listarPorEstado(
                        EstadoPrestamo.ATRASADO
                )
        ).thenReturn(
                prestamos
        );

        ResponseEntity<List<PrestamoResponseDTO>> respuesta =
                prestamoController.listarPorEstado(
                        EstadoPrestamo.ATRASADO
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                prestamos,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).listarPorEstado(
                EstadoPrestamo.ATRASADO
        );
    }

    @Test
    void devolverPrestamo_deberiaRetornarOkYPrestamoDevuelto() {

        DevolverPrestamoRequestDTO requestDTO =
                new DevolverPrestamoRequestDTO(
                        "Devuelto en buen estado"
                );

        PrestamoResponseDTO respuestaEsperada =
                crearPrestamoResponse(
                        7L,
                        18L,
                        32L,
                        EstadoPrestamo.DEVUELTO
                );

        when(
                prestamoService.devolverPrestamo(
                        7L,
                        requestDTO
                )
        ).thenReturn(
                respuestaEsperada
        );

        ResponseEntity<PrestamoResponseDTO> respuesta =
                prestamoController.devolverPrestamo(
                        7L,
                        requestDTO
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertSame(
                respuestaEsperada,
                respuesta.getBody()
        );

        verify(
                prestamoService
        ).devolverPrestamo(
                7L,
                requestDTO
        );
    }

    @Test
    void actualizarPrestamosAtrasados_deberiaRetornarCantidadActualizada() {

        when(
                prestamoService.actualizarPrestamosAtrasados()
        ).thenReturn(
                3
        );

        ResponseEntity<ActualizacionAtrasosResponseDTO> respuesta =
                prestamoController.actualizarPrestamosAtrasados();

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertEquals(
                3,
                respuesta.getBody().getPrestamosActualizados()
        );

        assertEquals(
                "Actualización de préstamos atrasados completada",
                respuesta.getBody().getMensaje()
        );

        verify(
                prestamoService
        ).actualizarPrestamosAtrasados();
    }

    private PrestamoResponseDTO crearPrestamoResponse(
            Long id,
            Long usuarioId,
            Long copiaId,
            EstadoPrestamo estado
    ) {
        return PrestamoResponseDTO.builder()
                .id(id)
                .usuarioId(usuarioId)
                .copiaId(copiaId)
                .fechaPrestamo(LocalDate.now())
                .fechaVencimiento(
                        LocalDate.now().plusDays(7)
                )
                .estado(estado)
                .diasAtraso(0L)
                .build();
    }
}
