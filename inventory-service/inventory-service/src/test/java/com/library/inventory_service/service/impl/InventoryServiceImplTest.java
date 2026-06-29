package com.library.inventory_service.service.impl;

import com.library.inventory_service.client.BookClient;
import java.util.List;
import java.util.Optional;
import com.library.inventory_service.client.CopiaClient;
import com.library.inventory_service.dto.BookResponseDTO;
import com.library.inventory_service.exception.RemoteServiceException;
import com.library.inventory_service.exception.ResourceNotFoundException;
import feign.FeignException;
import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.dto.InventoryMovementResponseDTO;
import com.library.inventory_service.exception.BusinessException;
import com.library.inventory_service.model.InventoryMovement;
import com.library.inventory_service.model.TipoMovimiento;
import com.library.inventory_service.repository.InventoryMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private BookClient bookClient;

    @Mock
    private CopiaClient copiaClient;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(
                inventoryMovementRepository,
                bookClient,
                copiaClient
        );
    }

    @Test
    void registrarEntradaCorrectamente() {
        InventoryMovementRequestDTO request =
                new InventoryMovementRequestDTO(
                        1L,
                        TipoMovimiento.ENTRADA,
                        5,
                        "Ingreso de nuevas copias",
                        "Bastian"
                );

        BookResponseDTO libro =
                mock(BookResponseDTO.class);

        when(libro.getTitle())
                .thenReturn("Historia de Chile");

        when(libro.getAuthor())
                .thenReturn("Francisco Antonio Encina");

        InventoryMovement movimientoGuardado =
                new InventoryMovement(
                        10L,
                        1L,
                        TipoMovimiento.ENTRADA,
                        5,
                        "Ingreso de nuevas copias",
                        "Bastian",
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                12,
                                0
                        )
                );

        when(bookClient.buscarLibroPorId(1L))
                .thenReturn(libro);

        when(inventoryMovementRepository.save(
                any(InventoryMovement.class)
        )).thenReturn(movimientoGuardado);

        when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
                .thenReturn(5L);

        InventoryMovementResponseDTO resultado =
                inventoryService.registrarMovimiento(request);

        assertNotNull(resultado);

        verify(bookClient, times(2))
                .buscarLibroPorId(1L);

        verify(inventoryMovementRepository)
                .save(any(InventoryMovement.class));

        verify(copiaClient)
                .contarCopiasDisponiblesPorLibro(1L);
    }

    @Test
    void registrarMovimientoConSolicitudNulaLanzaBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> inventoryService.registrarMovimiento(null)
        );

        assertEquals(
                "La solicitud de movimiento no puede ser nula",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryMovementRepository,
                bookClient,
                copiaClient
        );
    }

    @Test
    void registrarSalidaSinCopiasSuficientesLanzaBusinessException() {
        InventoryMovementRequestDTO request =
                new InventoryMovementRequestDTO(
                        1L,
                        TipoMovimiento.SALIDA,
                        3,
                        "Retiro de copias",
                        "Bastian"
                );

        BookResponseDTO libro =
                mock(BookResponseDTO.class);

        when(bookClient.buscarLibroPorId(1L))
                .thenReturn(libro);

        when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
                .thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> inventoryService.registrarMovimiento(request)
        );

        assertEquals(
                "No hay suficientes copias disponibles para registrar este movimiento",
                exception.getMessage()
        );

        verify(bookClient)
                .buscarLibroPorId(1L);

        verify(copiaClient)
                .contarCopiasDisponiblesPorLibro(1L);

        verify(inventoryMovementRepository, never())
                .save(any(InventoryMovement.class));
    }
    @Test
void registrarMovimientoConLibroInexistenteLanzaResourceNotFoundException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    999L,
                    TipoMovimiento.ENTRADA,
                    2,
                    "Ingreso de copias",
                    "Bastian"
            );

    FeignException feignException =
            mock(FeignException.class);

    when(feignException.status())
            .thenReturn(404);

    when(bookClient.buscarLibroPorId(999L))
            .thenThrow(feignException);

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No existe libro en book-service con ID: 999",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(999L);

    verifyNoInteractions(
            inventoryMovementRepository,
            copiaClient
    );
}

@Test
void registrarMovimientoCuandoBookServiceFallaLanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.ENTRADA,
                    2,
                    "Ingreso de copias",
                    "Bastian"
            );

    when(bookClient.buscarLibroPorId(1L))
            .thenThrow(
                    new RuntimeException(
                            "book-service no disponible"
                    )
            );

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No fue posible comunicarse con book-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verifyNoInteractions(
            inventoryMovementRepository,
            copiaClient
    );
}

@Test
void registrarMovimientoCuandoBookServiceDevuelveNuloLanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.ENTRADA,
                    2,
                    "Ingreso de copias",
                    "Bastian"
            );

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(null);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "book-service devolvió una respuesta vacía",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verifyNoInteractions(
            inventoryMovementRepository,
            copiaClient
    );
}

@Test
void registrarSalidaCuandoCopiaServiceFallaLanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.SALIDA,
                    2,
                    "Retiro de copias",
                    "Bastian"
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenThrow(
                    new RuntimeException(
                            "copia-service no disponible"
                    )
            );

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No fue posible comunicarse con copia-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);

    verify(inventoryMovementRepository, never())
            .save(any(InventoryMovement.class));
}
@Test
void registrarSalidaCuandoCopiaServiceDevuelveNuloLanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.SALIDA,
                    2,
                    "Retiro de copias",
                    "Bastian"
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenReturn(null);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "copia-service devolvió una respuesta vacía",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);

    verify(inventoryMovementRepository, never())
            .save(any(InventoryMovement.class));
}

@Test
void buscarMovimientoPorIdCorrectamente() {
    InventoryMovement movimiento =
            new InventoryMovement(
                    10L,
                    1L,
                    TipoMovimiento.ENTRADA,
                    5,
                    "Ingreso de copias",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            13,
                            0
                    )
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("Historia de Chile");

    when(libro.getAuthor())
            .thenReturn("Francisco Antonio Encina");

    when(inventoryMovementRepository.findById(10L))
            .thenReturn(Optional.of(movimiento));

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenReturn(5L);

    InventoryMovementResponseDTO resultado =
            inventoryService.buscarMovimientoPorId(10L);

    assertNotNull(resultado);
    assertEquals(10L, resultado.getId());
    assertEquals(1L, resultado.getBookId());
    assertEquals(
            TipoMovimiento.ENTRADA,
            resultado.getTipoMovimiento()
    );
    assertEquals(5, resultado.getCantidad());

    verify(inventoryMovementRepository)
            .findById(10L);

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);
}

@Test
void buscarMovimientoPorIdInexistenteLanzaResourceNotFoundException() {
    when(inventoryMovementRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.buscarMovimientoPorId(999L)
    );

    assertEquals(
            "No existe movimiento de inventario con ID: 999",
            exception.getMessage()
    );

    verify(inventoryMovementRepository)
            .findById(999L);

    verifyNoInteractions(
            bookClient,
            copiaClient
    );
}

@Test
void listarMovimientosCorrectamente() {
    InventoryMovement movimiento =
            new InventoryMovement(
                    10L,
                    1L,
                    TipoMovimiento.ENTRADA,
                    5,
                    "Ingreso de copias",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            13,
                            10
                    )
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("Historia de Chile");

    when(libro.getAuthor())
            .thenReturn("Francisco Antonio Encina");

    when(inventoryMovementRepository.findAll())
            .thenReturn(List.of(movimiento));

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenReturn(5L);

    List<InventoryMovementResponseDTO> resultado =
            inventoryService.listarMovimientos();

    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals(10L, resultado.get(0).getId());
    assertEquals(
            TipoMovimiento.ENTRADA,
            resultado.get(0).getTipoMovimiento()
    );

    verify(inventoryMovementRepository)
            .findAll();

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);
}
@Test
void listarMovimientosPorLibroCorrectamente() {
    InventoryMovement movimiento =
            new InventoryMovement(
                    20L,
                    2L,
                    TipoMovimiento.ENTRADA,
                    4,
                    "Ingreso de ejemplares",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            14,
                            0
                    )
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("El Principito");

    when(libro.getAuthor())
            .thenReturn("Antoine de Saint-Exupéry");

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    when(inventoryMovementRepository.findByBookId(2L))
            .thenReturn(List.of(movimiento));

    when(copiaClient.contarCopiasDisponiblesPorLibro(2L))
            .thenReturn(4L);

    List<InventoryMovementResponseDTO> resultado =
            inventoryService.listarMovimientosPorLibro(2L);

    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals(20L, resultado.get(0).getId());
    assertEquals(2L, resultado.get(0).getBookId());
    assertEquals(
            TipoMovimiento.ENTRADA,
            resultado.get(0).getTipoMovimiento()
    );

    verify(bookClient, times(2))
            .buscarLibroPorId(2L);

    verify(inventoryMovementRepository)
            .findByBookId(2L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(2L);
}

@Test
void listarMovimientosPorTipoCorrectamente() {
    InventoryMovement movimiento =
            new InventoryMovement(
                    21L,
                    2L,
                    TipoMovimiento.PERDIDO,
                    1,
                    "Ejemplar extraviado",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            14,
                            10
                    )
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("El Principito");

    when(libro.getAuthor())
            .thenReturn("Antoine de Saint-Exupéry");

    when(inventoryMovementRepository.findByTipoMovimiento(
            TipoMovimiento.PERDIDO
    )).thenReturn(List.of(movimiento));

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(2L))
            .thenReturn(3L);

    List<InventoryMovementResponseDTO> resultado =
            inventoryService.listarMovimientosPorTipo(
                    TipoMovimiento.PERDIDO
            );

    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals(21L, resultado.get(0).getId());
    assertEquals(
            TipoMovimiento.PERDIDO,
            resultado.get(0).getTipoMovimiento()
    );
    assertEquals(1, resultado.get(0).getCantidad());

    verify(inventoryMovementRepository)
            .findByTipoMovimiento(
                    TipoMovimiento.PERDIDO
            );

    verify(bookClient)
            .buscarLibroPorId(2L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(2L);
}

@Test
void obtenerResumenPorLibroCorrectamente() {
    InventoryMovement entrada =
            new InventoryMovement(
                    30L,
                    2L,
                    TipoMovimiento.ENTRADA,
                    5,
                    "Ingreso de ejemplares",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            14,
                            20
                    )
            );

    InventoryMovement salida =
            new InventoryMovement(
                    31L,
                    2L,
                    TipoMovimiento.SALIDA,
                    2,
                    "Retiro de ejemplares",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            14,
                            30
                    )
            );

    InventoryMovement danado =
            new InventoryMovement(
                    32L,
                    2L,
                    TipoMovimiento.DANADO,
                    1,
                    "Ejemplar dañado",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            14,
                            40
                    )
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("El Principito");

    when(libro.getAuthor())
            .thenReturn("Antoine de Saint-Exupéry");

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(2L))
            .thenReturn(2L);

    when(inventoryMovementRepository.findByBookId(2L))
            .thenReturn(
                    List.of(
                            entrada,
                            salida,
                            danado
                    )
            );

    assertNotNull(
            inventoryService.obtenerResumenPorLibro(2L)
    );

    verify(bookClient)
            .buscarLibroPorId(2L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(2L);

    verify(inventoryMovementRepository)
            .findByBookId(2L);
}

@Test
void eliminarMovimientoCorrectamente() {
    InventoryMovement movimiento =
            new InventoryMovement(
                    40L,
                    2L,
                    TipoMovimiento.REVISION,
                    1,
                    "Revisión de inventario",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            15,
                            0
                    )
            );

    when(inventoryMovementRepository.findById(40L))
            .thenReturn(Optional.of(movimiento));

    inventoryService.eliminarMovimiento(40L);

    verify(inventoryMovementRepository)
            .findById(40L);

    verify(inventoryMovementRepository)
            .delete(movimiento);

    verifyNoInteractions(
            bookClient,
            copiaClient
    );
}
@Test
void listarMovimientosSinRegistrosDevuelveListaVacia() {
    when(inventoryMovementRepository.findAll())
            .thenReturn(List.of());

    List<InventoryMovementResponseDTO> resultado =
            inventoryService.listarMovimientos();

    assertNotNull(resultado);
    assertEquals(0, resultado.size());

    verify(inventoryMovementRepository)
            .findAll();

    verifyNoInteractions(
            bookClient,
            copiaClient
    );
}

@Test
void listarMovimientosPorLibroInexistenteLanzaResourceNotFoundException() {
    FeignException feignException =
            mock(FeignException.class);

    when(feignException.status())
            .thenReturn(404);

    when(bookClient.buscarLibroPorId(999L))
            .thenThrow(feignException);

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.listarMovimientosPorLibro(999L)
    );

    assertEquals(
            "No existe libro en book-service con ID: 999",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(999L);

    verifyNoInteractions(
            inventoryMovementRepository,
            copiaClient
    );
}

@Test
void listarMovimientosPorTipoSinResultadosDevuelveListaVacia() {
    when(inventoryMovementRepository.findByTipoMovimiento(
            TipoMovimiento.REVISION
    )).thenReturn(List.of());

    List<InventoryMovementResponseDTO> resultado =
            inventoryService.listarMovimientosPorTipo(
                    TipoMovimiento.REVISION
            );

    assertNotNull(resultado);
    assertEquals(0, resultado.size());

    verify(inventoryMovementRepository)
            .findByTipoMovimiento(
                    TipoMovimiento.REVISION
            );

    verifyNoInteractions(
            bookClient,
            copiaClient
    );
}

@Test
void eliminarMovimientoInexistenteLanzaResourceNotFoundException() {
    when(inventoryMovementRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.eliminarMovimiento(999L)
    );

    assertEquals(
            "No existe movimiento de inventario con ID: 999",
            exception.getMessage()
    );

    verify(inventoryMovementRepository)
            .findById(999L);

    verify(inventoryMovementRepository, never())
            .delete(any(InventoryMovement.class));

    verifyNoInteractions(
            bookClient,
            copiaClient
    );
}
@Test
void registrarMovimientoCuandoBookServiceDevuelve500LanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.ENTRADA,
                    2,
                    "Ingreso de ejemplares",
                    "Bastian"
            );

    FeignException feignException =
            mock(FeignException.class);

    when(feignException.status())
            .thenReturn(500);

    when(bookClient.buscarLibroPorId(1L))
            .thenThrow(feignException);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No fue posible comunicarse con book-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verifyNoInteractions(
            inventoryMovementRepository,
            copiaClient
    );
}

@Test
void registrarSalidaCuandoCopiaServiceDevuelve500LanzaRemoteServiceException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.SALIDA,
                    2,
                    "Retiro de ejemplares",
                    "Bastian"
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    FeignException feignException =
            mock(FeignException.class);

    when(feignException.status())
            .thenReturn(500);

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenThrow(feignException);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No fue posible comunicarse con copia-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);

    verify(inventoryMovementRepository, never())
            .save(any(InventoryMovement.class));
}

@Test
void registrarMovimientoDanadoSinCopiasSuficientesLanzaBusinessException() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.DANADO,
                    3,
                    "Ejemplares dañados",
                    "Bastian"
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenReturn(2L);

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> inventoryService.registrarMovimiento(request)
    );

    assertEquals(
            "No hay suficientes copias disponibles para registrar este movimiento",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(1L);

    verify(copiaClient)
            .contarCopiasDisponiblesPorLibro(1L);

    verify(inventoryMovementRepository, never())
            .save(any(InventoryMovement.class));
}

@Test
void registrarMovimientoPerdidoCorrectamente() {
    InventoryMovementRequestDTO request =
            new InventoryMovementRequestDTO(
                    1L,
                    TipoMovimiento.PERDIDO,
                    1,
                    "Ejemplar extraviado",
                    "Bastian"
            );

    BookResponseDTO libro =
            mock(BookResponseDTO.class);

    when(libro.getTitle())
            .thenReturn("Historia de Chile");

    when(libro.getAuthor())
            .thenReturn("Francisco Antonio Encina");

    InventoryMovement movimientoGuardado =
            new InventoryMovement(
                    50L,
                    1L,
                    TipoMovimiento.PERDIDO,
                    1,
                    "Ejemplar extraviado",
                    "Bastian",
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            16,
                            0
                    )
            );

    when(bookClient.buscarLibroPorId(1L))
            .thenReturn(libro);

    when(copiaClient.contarCopiasDisponiblesPorLibro(1L))
            .thenReturn(3L);

    when(inventoryMovementRepository.save(
            any(InventoryMovement.class)
    )).thenReturn(movimientoGuardado);

    InventoryMovementResponseDTO resultado =
            inventoryService.registrarMovimiento(request);

    assertNotNull(resultado);
    assertEquals(50L, resultado.getId());
    assertEquals(
            TipoMovimiento.PERDIDO,
            resultado.getTipoMovimiento()
    );
    assertEquals(1, resultado.getCantidad());

    verify(bookClient, times(2))
            .buscarLibroPorId(1L);

    verify(copiaClient, times(2))
            .contarCopiasDisponiblesPorLibro(1L);

    verify(inventoryMovementRepository)
            .save(any(InventoryMovement.class));
}
}