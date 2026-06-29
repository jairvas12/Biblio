package com.library.prestamo_service.service.impl;

import com.library.prestamo_service.client.CopiaClient;
import com.library.prestamo_service.client.dto.CopiaRemotaDTO;
import com.library.prestamo_service.client.dto.EstadoCopiaRemota;
import com.library.prestamo_service.dto.CrearPrestamoRequestDTO;
import com.library.prestamo_service.dto.DevolverPrestamoRequestDTO;
import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.exception.RecursoNoEncontradoException;
import com.library.prestamo_service.exception.ReglaNegocioException;
import com.library.prestamo_service.exception.ServicioRemotoException;
import com.library.prestamo_service.mapper.PrestamoMapper;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.model.Prestamo;
import com.library.prestamo_service.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceImplTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private PrestamoMapper prestamoMapper;

    @Mock
    private CopiaClient copiaClient;

    @InjectMocks
    private PrestamoServiceImpl prestamoService;

    @Test
    void crearPrestamo_deberiaCrearPrestamoCuandoLosDatosSonValidos() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "  Préstamo de prueba  "
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                );

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.LOANED
                );

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(7)
                        )
                        .estado(EstadoPrestamo.ACTIVO)
                        .observacion("Préstamo de prueba")
                        .diasAtraso(0L)
                        .build();

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.existsByCopiaIdAndEstadoIn(
                        eq(20L),
                        anyCollection()
                )
        ).thenReturn(false);

        when(
                prestamoRepository.countByUsuarioIdAndEstadoIn(
                        eq(10L),
                        anyCollection()
                )
        ).thenReturn(0L);

        when(
                copiaClient.cambiarEstado(
                        20L,
                        EstadoCopiaRemota.LOANED
                )
        ).thenReturn(
                copiaPrestada
        );

        when(
                prestamoRepository.saveAndFlush(
                        any(Prestamo.class)
                )
        ).thenAnswer(invocacion -> {

            Prestamo prestamo =
                    invocacion.getArgument(0);

            prestamo.setId(1L);

            return prestamo;
        });

        when(
                prestamoMapper.convertirAResponseDTO(
                        any(Prestamo.class)
                )
        ).thenReturn(
                respuestaEsperada
        );

        PrestamoResponseDTO resultado =
                prestamoService.crearPrestamo(
                        requestDTO
                );

        ArgumentCaptor<Prestamo> captor =
                ArgumentCaptor.forClass(
                        Prestamo.class
                );

        verify(
                prestamoRepository
        ).saveAndFlush(
                captor.capture()
        );

        Prestamo prestamoGuardado =
                captor.getValue();

        assertSame(
                respuestaEsperada,
                resultado
        );

        assertAll(
                () -> assertEquals(
                        10L,
                        prestamoGuardado.getUsuarioId()
                ),
                () -> assertEquals(
                        20L,
                        prestamoGuardado.getCopiaId()
                ),
                () -> assertEquals(
                        LocalDate.now(),
                        prestamoGuardado.getFechaPrestamo()
                ),
                () -> assertEquals(
                        LocalDate.now().plusDays(7),
                        prestamoGuardado.getFechaVencimiento()
                ),
                () -> assertEquals(
                        EstadoPrestamo.ACTIVO,
                        prestamoGuardado.getEstado()
                ),
                () -> assertEquals(
                        "Préstamo de prueba",
                        prestamoGuardado.getObservacion()
                ),
                () -> assertNull(
                        prestamoGuardado.getFechaDevolucion()
                )
        );

        verify(
                copiaClient
        ).cambiarEstado(
                20L,
                EstadoCopiaRemota.LOANED
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                any(Prestamo.class)
        );
    }

    @Test
    void crearPrestamo_deberiaLanzarExcepcionCuandoCopiaNoEstaDisponible() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba"
                );

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.LOANED
                );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaPrestada
        );

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verifyNoInteractions(
                prestamoRepository,
                prestamoMapper
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );
    }

    @Test
    void crearPrestamo_deberiaLanzarExcepcionCuandoCopiaTienePrestamoVigente() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba"
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.existsByCopiaIdAndEstadoIn(
                        eq(20L),
                        anyCollection()
                )
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verify(
                prestamoRepository,
                never()
        ).countByUsuarioIdAndEstadoIn(
                anyLong(),
                anyCollection()
        );

        verify(
                prestamoRepository,
                never()
        ).saveAndFlush(
                any(Prestamo.class)
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );

        verifyNoInteractions(
                prestamoMapper
        );
    }

    @Test
    void crearPrestamo_deberiaLanzarExcepcionCuandoUsuarioAlcanzoLimite() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba de límite"
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.existsByCopiaIdAndEstadoIn(
                        eq(20L),
                        anyCollection()
                )
        ).thenReturn(false);

        when(
                prestamoRepository.countByUsuarioIdAndEstadoIn(
                        eq(10L),
                        anyCollection()
                )
        ).thenReturn(3L);

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );

        verify(
                prestamoRepository,
                never()
        ).saveAndFlush(
                any(Prestamo.class)
        );

        verifyNoInteractions(
                prestamoMapper
        );
    }

    @Test
    void crearPrestamo_deberiaLanzarExcepcionCuandoCopiaServiceRespondeIncompleto() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba de respuesta incompleta"
                );

        CopiaRemotaDTO copiaIncompleta =
                new CopiaRemotaDTO();

        copiaIncompleta.setId(20L);
        copiaIncompleta.setEstado(null);

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaIncompleta
        );

        assertThrows(
                ServicioRemotoException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verifyNoInteractions(
                prestamoRepository,
                prestamoMapper
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );
    }

    @Test
    void crearPrestamo_deberiaLanzarExcepcionCuandoCopiaServiceDevuelveNull() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba con respuesta nula"
                );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(null);

        assertThrows(
                ServicioRemotoException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verifyNoInteractions(
                prestamoRepository,
                prestamoMapper
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );
    }

    @Test
    void crearPrestamo_deberiaRestaurarCopiaCuandoFallaGuardado() {

        CrearPrestamoRequestDTO requestDTO =
                new CrearPrestamoRequestDTO(
                        10L,
                        20L,
                        7,
                        "Prueba de compensación"
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                );

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.LOANED
                );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.existsByCopiaIdAndEstadoIn(
                        eq(20L),
                        anyCollection()
                )
        ).thenReturn(false);

        when(
                prestamoRepository.countByUsuarioIdAndEstadoIn(
                        eq(10L),
                        anyCollection()
                )
        ).thenReturn(0L);

        when(
                copiaClient.cambiarEstado(
                        20L,
                        EstadoCopiaRemota.LOANED
                )
        ).thenReturn(
                copiaPrestada
        );

        when(
                copiaClient.cambiarEstado(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                )
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.saveAndFlush(
                        any(Prestamo.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error simulado al guardar"
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> prestamoService.crearPrestamo(
                        requestDTO
                )
        );

        verify(
                copiaClient
        ).cambiarEstado(
                20L,
                EstadoCopiaRemota.LOANED
        );

        verify(
                copiaClient
        ).cambiarEstado(
                20L,
                EstadoCopiaRemota.AVAILABLE
        );

        verifyNoInteractions(
                prestamoMapper
        );
    }


    @Test
    void obtenerPorId_deberiaRetornarPrestamoCuandoExiste() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(7)
                        )
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        when(
                prestamoRepository.findById(1L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        PrestamoResponseDTO resultado =
                prestamoService.obtenerPorId(1L);

        assertSame(
                respuestaEsperada,
                resultado
        );

        verify(
                prestamoRepository
        ).findById(1L);

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }

    @Test
    void obtenerPorId_deberiaLanzarExcepcionCuandoNoExiste() {

        when(
                prestamoRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> prestamoService.obtenerPorId(99L)
        );

        verify(
                prestamoRepository
        ).findById(99L);

        verifyNoInteractions(
                prestamoMapper
        );
    }


    @Test
    void listarTodos_deberiaRetornarTodosLosPrestamos() {

        Prestamo prestamoUno =
                Prestamo.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(7)
                        )
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        Prestamo prestamoDos =
                Prestamo.builder()
                        .id(2L)
                        .usuarioId(11L)
                        .copiaId(21L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(10)
                        )
                        .estado(EstadoPrestamo.DEVUELTO)
                        .build();

        PrestamoResponseDTO respuestaUno =
                PrestamoResponseDTO.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        PrestamoResponseDTO respuestaDos =
                PrestamoResponseDTO.builder()
                        .id(2L)
                        .usuarioId(11L)
                        .copiaId(21L)
                        .estado(EstadoPrestamo.DEVUELTO)
                        .build();

        when(
                prestamoRepository.findAll()
        ).thenReturn(
                List.of(
                        prestamoUno,
                        prestamoDos
                )
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamoUno
                )
        ).thenReturn(
                respuestaUno
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamoDos
                )
        ).thenReturn(
                respuestaDos
        );

        List<PrestamoResponseDTO> resultado =
                prestamoService.listarTodos();

        assertEquals(
                List.of(
                        respuestaUno,
                        respuestaDos
                ),
                resultado
        );

        verify(
                prestamoRepository
        ).findAll();

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamoUno
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamoDos
        );
    }

    @Test
    void listarPorUsuario_deberiaRetornarPrestamosDelUsuario() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(3L)
                        .usuarioId(10L)
                        .copiaId(22L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(5)
                        )
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(3L)
                        .usuarioId(10L)
                        .copiaId(22L)
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        when(
                prestamoRepository
                        .findByUsuarioIdOrderByFechaPrestamoDesc(
                                10L
                        )
        ).thenReturn(
                List.of(prestamo)
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        List<PrestamoResponseDTO> resultado =
                prestamoService.listarPorUsuario(10L);

        assertEquals(
                List.of(respuestaEsperada),
                resultado
        );

        verify(
                prestamoRepository
        ).findByUsuarioIdOrderByFechaPrestamoDesc(
                10L
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }


    @Test
    void listarPorCopia_deberiaRetornarPrestamosDeLaCopia() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(4L)
                        .usuarioId(12L)
                        .copiaId(30L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaVencimiento(
                                LocalDate.now().plusDays(8)
                        )
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(4L)
                        .usuarioId(12L)
                        .copiaId(30L)
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        when(
                prestamoRepository
                        .findByCopiaIdOrderByFechaPrestamoDesc(
                                30L
                        )
        ).thenReturn(
                List.of(prestamo)
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        List<PrestamoResponseDTO> resultado =
                prestamoService.listarPorCopia(30L);

        assertEquals(
                List.of(respuestaEsperada),
                resultado
        );

        verify(
                prestamoRepository
        ).findByCopiaIdOrderByFechaPrestamoDesc(
                30L
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }

    @Test
    void listarPorEstado_deberiaRetornarPrestamosConElEstadoIndicado() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(5L)
                        .usuarioId(13L)
                        .copiaId(31L)
                        .fechaPrestamo(LocalDate.now().minusDays(10))
                        .fechaVencimiento(LocalDate.now().minusDays(3))
                        .estado(EstadoPrestamo.ATRASADO)
                        .build();

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(5L)
                        .usuarioId(13L)
                        .copiaId(31L)
                        .estado(EstadoPrestamo.ATRASADO)
                        .build();

        when(
                prestamoRepository
                        .findByEstadoOrderByFechaVencimientoAsc(
                                EstadoPrestamo.ATRASADO
                        )
        ).thenReturn(
                List.of(prestamo)
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        List<PrestamoResponseDTO> resultado =
                prestamoService.listarPorEstado(
                        EstadoPrestamo.ATRASADO
                );

        assertEquals(
                List.of(respuestaEsperada),
                resultado
        );

        verify(
                prestamoRepository
        ).findByEstadoOrderByFechaVencimientoAsc(
                EstadoPrestamo.ATRASADO
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }


    @Test
    void devolverPrestamo_deberiaDevolverPrestamoActivoCorrectamente() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaPrestamo(LocalDate.now().minusDays(3))
                        .fechaVencimiento(LocalDate.now().plusDays(4))
                        .fechaDevolucion(null)
                        .estado(EstadoPrestamo.ACTIVO)
                        .observacion("Préstamo activo")
                        .build();

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.LOANED
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                );

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(1L)
                        .usuarioId(10L)
                        .copiaId(20L)
                        .fechaDevolucion(LocalDate.now())
                        .estado(EstadoPrestamo.DEVUELTO)
                        .observacion("Préstamo activo")
                        .build();

        when(
                prestamoRepository.findById(1L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        when(
                copiaClient.obtenerCopiaPorId(20L)
        ).thenReturn(
                copiaPrestada
        );

        when(
                copiaClient.cambiarEstado(
                        20L,
                        EstadoCopiaRemota.AVAILABLE
                )
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.saveAndFlush(
                        prestamo
                )
        ).thenReturn(
                prestamo
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        PrestamoResponseDTO resultado =
                prestamoService.devolverPrestamo(
                        1L,
                        null
                );

        assertSame(
                respuestaEsperada,
                resultado
        );

        assertAll(
                () -> assertEquals(
                        EstadoPrestamo.DEVUELTO,
                        prestamo.getEstado()
                ),
                () -> assertEquals(
                        LocalDate.now(),
                        prestamo.getFechaDevolucion()
                ),
                () -> assertEquals(
                        "Préstamo activo",
                        prestamo.getObservacion()
                )
        );

        verify(
                copiaClient
        ).cambiarEstado(
                20L,
                EstadoCopiaRemota.AVAILABLE
        );

        verify(
                prestamoRepository
        ).saveAndFlush(
                prestamo
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }

    @Test
    void devolverPrestamo_deberiaLanzarExcepcionCuandoYaFueDevuelto() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(2L)
                        .usuarioId(10L)
                        .copiaId(21L)
                        .fechaPrestamo(LocalDate.now().minusDays(10))
                        .fechaVencimiento(LocalDate.now().minusDays(3))
                        .fechaDevolucion(LocalDate.now().minusDays(1))
                        .estado(EstadoPrestamo.DEVUELTO)
                        .build();

        when(
                prestamoRepository.findById(2L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.devolverPrestamo(
                        2L,
                        null
                )
        );

        verify(
                prestamoRepository,
                never()
        ).saveAndFlush(
                any(Prestamo.class)
        );

        verifyNoInteractions(
                copiaClient,
                prestamoMapper
        );
    }


    @Test
    void devolverPrestamo_deberiaLanzarExcepcionCuandoPrestamoEstaCancelado() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(3L)
                        .usuarioId(10L)
                        .copiaId(22L)
                        .fechaPrestamo(LocalDate.now().minusDays(5))
                        .fechaVencimiento(LocalDate.now().plusDays(2))
                        .estado(EstadoPrestamo.CANCELADO)
                        .build();

        when(
                prestamoRepository.findById(3L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.devolverPrestamo(
                        3L,
                        null
                )
        );

        verify(
                prestamoRepository,
                never()
        ).saveAndFlush(
                any(Prestamo.class)
        );

        verifyNoInteractions(
                copiaClient,
                prestamoMapper
        );
    }

    @Test
    void devolverPrestamo_deberiaLanzarExcepcionCuandoCopiaNoEstaPrestada() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(4L)
                        .usuarioId(11L)
                        .copiaId(23L)
                        .fechaPrestamo(LocalDate.now().minusDays(2))
                        .fechaVencimiento(LocalDate.now().plusDays(5))
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        23L,
                        EstadoCopiaRemota.AVAILABLE
                );

        when(
                prestamoRepository.findById(4L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        when(
                copiaClient.obtenerCopiaPorId(23L)
        ).thenReturn(
                copiaDisponible
        );

        assertThrows(
                ReglaNegocioException.class,
                () -> prestamoService.devolverPrestamo(
                        4L,
                        null
                )
        );

        verify(
                copiaClient,
                never()
        ).cambiarEstado(
                anyLong(),
                any(EstadoCopiaRemota.class)
        );

        verify(
                prestamoRepository,
                never()
        ).saveAndFlush(
                any(Prestamo.class)
        );

        verifyNoInteractions(
                prestamoMapper
        );
    }


    @Test
    void devolverPrestamo_deberiaActualizarObservacionCuandoSeEnvia() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(5L)
                        .usuarioId(12L)
                        .copiaId(24L)
                        .fechaPrestamo(LocalDate.now().minusDays(4))
                        .fechaVencimiento(LocalDate.now().plusDays(3))
                        .fechaDevolucion(null)
                        .estado(EstadoPrestamo.ACTIVO)
                        .observacion("Observación anterior")
                        .build();

        DevolverPrestamoRequestDTO requestDTO =
                new DevolverPrestamoRequestDTO(
                        "  Devuelto en buen estado  "
                );

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        24L,
                        EstadoCopiaRemota.LOANED
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        24L,
                        EstadoCopiaRemota.AVAILABLE
                );

        PrestamoResponseDTO respuestaEsperada =
                PrestamoResponseDTO.builder()
                        .id(5L)
                        .usuarioId(12L)
                        .copiaId(24L)
                        .fechaDevolucion(LocalDate.now())
                        .estado(EstadoPrestamo.DEVUELTO)
                        .observacion("Devuelto en buen estado")
                        .build();

        when(
                prestamoRepository.findById(5L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        when(
                copiaClient.obtenerCopiaPorId(24L)
        ).thenReturn(
                copiaPrestada
        );

        when(
                copiaClient.cambiarEstado(
                        24L,
                        EstadoCopiaRemota.AVAILABLE
                )
        ).thenReturn(
                copiaDisponible
        );

        when(
                prestamoRepository.saveAndFlush(
                        prestamo
                )
        ).thenReturn(
                prestamo
        );

        when(
                prestamoMapper.convertirAResponseDTO(
                        prestamo
                )
        ).thenReturn(
                respuestaEsperada
        );

        PrestamoResponseDTO resultado =
                prestamoService.devolverPrestamo(
                        5L,
                        requestDTO
                );

        assertSame(
                respuestaEsperada,
                resultado
        );

        assertAll(
                () -> assertEquals(
                        EstadoPrestamo.DEVUELTO,
                        prestamo.getEstado()
                ),
                () -> assertEquals(
                        LocalDate.now(),
                        prestamo.getFechaDevolucion()
                ),
                () -> assertEquals(
                        "Devuelto en buen estado",
                        prestamo.getObservacion()
                )
        );

        verify(
                copiaClient
        ).cambiarEstado(
                24L,
                EstadoCopiaRemota.AVAILABLE
        );

        verify(
                prestamoRepository
        ).saveAndFlush(
                prestamo
        );

        verify(
                prestamoMapper
        ).convertirAResponseDTO(
                prestamo
        );
    }

    @Test
    void devolverPrestamo_deberiaRestaurarDatosYCopiaCuandoFallaGuardado() {

        Prestamo prestamo =
                Prestamo.builder()
                        .id(6L)
                        .usuarioId(13L)
                        .copiaId(25L)
                        .fechaPrestamo(LocalDate.now().minusDays(12))
                        .fechaVencimiento(LocalDate.now().minusDays(5))
                        .fechaDevolucion(null)
                        .estado(EstadoPrestamo.ATRASADO)
                        .observacion("Observación original")
                        .build();

        DevolverPrestamoRequestDTO requestDTO =
                new DevolverPrestamoRequestDTO(
                        "Nueva observación"
                );

        CopiaRemotaDTO copiaPrestada =
                crearCopia(
                        25L,
                        EstadoCopiaRemota.LOANED
                );

        CopiaRemotaDTO copiaDisponible =
                crearCopia(
                        25L,
                        EstadoCopiaRemota.AVAILABLE
                );

        when(
                prestamoRepository.findById(6L)
        ).thenReturn(
                Optional.of(prestamo)
        );

        when(
                copiaClient.obtenerCopiaPorId(25L)
        ).thenReturn(
                copiaPrestada
        );

        when(
                copiaClient.cambiarEstado(
                        25L,
                        EstadoCopiaRemota.AVAILABLE
                )
        ).thenReturn(
                copiaDisponible
        );

        when(
                copiaClient.cambiarEstado(
                        25L,
                        EstadoCopiaRemota.LOANED
                )
        ).thenReturn(
                copiaPrestada
        );

        when(
                prestamoRepository.saveAndFlush(
                        prestamo
                )
        ).thenThrow(
                new RuntimeException(
                        "Error simulado al guardar devolución"
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> prestamoService.devolverPrestamo(
                        6L,
                        requestDTO
                )
        );

        assertAll(
                () -> assertEquals(
                        EstadoPrestamo.ATRASADO,
                        prestamo.getEstado()
                ),
                () -> assertNull(
                        prestamo.getFechaDevolucion()
                ),
                () -> assertEquals(
                        "Observación original",
                        prestamo.getObservacion()
                )
        );

        verify(
                copiaClient
        ).cambiarEstado(
                25L,
                EstadoCopiaRemota.AVAILABLE
        );

        verify(
                copiaClient
        ).cambiarEstado(
                25L,
                EstadoCopiaRemota.LOANED
        );

        verifyNoInteractions(
                prestamoMapper
        );
    }


    @Test
    void actualizarPrestamosAtrasados_deberiaRetornarCeroCuandoNoHayVencidos() {

        when(
                prestamoRepository
                        .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                                EstadoPrestamo.ACTIVO,
                                LocalDate.now()
                        )
        ).thenReturn(
                List.of()
        );

        int resultado =
                prestamoService.actualizarPrestamosAtrasados();

        assertEquals(
                0,
                resultado
        );

        verify(
                prestamoRepository
        ).findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                EstadoPrestamo.ACTIVO,
                LocalDate.now()
        );

        verify(
                prestamoRepository,
                never()
        ).saveAll(
                anyCollection()
        );
    }

    @Test
    void actualizarPrestamosAtrasados_deberiaMarcarYGuardarPrestamosVencidos() {

        Prestamo prestamoUno =
                Prestamo.builder()
                        .id(7L)
                        .usuarioId(14L)
                        .copiaId(26L)
                        .fechaPrestamo(LocalDate.now().minusDays(15))
                        .fechaVencimiento(LocalDate.now().minusDays(5))
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        Prestamo prestamoDos =
                Prestamo.builder()
                        .id(8L)
                        .usuarioId(15L)
                        .copiaId(27L)
                        .fechaPrestamo(LocalDate.now().minusDays(12))
                        .fechaVencimiento(LocalDate.now().minusDays(2))
                        .estado(EstadoPrestamo.ACTIVO)
                        .build();

        List<Prestamo> prestamosVencidos =
                List.of(
                        prestamoUno,
                        prestamoDos
                );

        when(
                prestamoRepository
                        .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                                EstadoPrestamo.ACTIVO,
                                LocalDate.now()
                        )
        ).thenReturn(
                prestamosVencidos
        );

        int resultado =
                prestamoService.actualizarPrestamosAtrasados();

        assertAll(
                () -> assertEquals(
                        2,
                        resultado
                ),
                () -> assertEquals(
                        EstadoPrestamo.ATRASADO,
                        prestamoUno.getEstado()
                ),
                () -> assertEquals(
                        EstadoPrestamo.ATRASADO,
                        prestamoDos.getEstado()
                )
        );

        verify(
                prestamoRepository
        ).saveAll(
                prestamosVencidos
        );
    }

    private CopiaRemotaDTO crearCopia(
            Long copiaId,
            EstadoCopiaRemota estado
    ) {
        CopiaRemotaDTO copia =
                new CopiaRemotaDTO();

        copia.setId(copiaId);
        copia.setEstado(estado);

        return copia;
    }
}
