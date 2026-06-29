package com.library.inventory_service.controller;

import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.dto.InventoryMovementResponseDTO;
import com.library.inventory_service.model.TipoMovimiento;
import com.library.inventory_service.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import com.library.inventory_service.dto.InventorySummaryDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        inventoryController =
                new InventoryController(inventoryService);
    }

    @Test
    void registrarMovimientoDevuelve201() {
        InventoryMovementRequestDTO request =
                new InventoryMovementRequestDTO(
                        1L,
                        TipoMovimiento.ENTRADA,
                        5,
                        "Ingreso de ejemplares",
                        "Bastian"
                );

        InventoryMovementResponseDTO respuesta =
                mock(InventoryMovementResponseDTO.class);

        when(inventoryService.registrarMovimiento(request))
                .thenReturn(respuesta);

        ResponseEntity<InventoryMovementResponseDTO> resultado =
                inventoryController.registrarMovimiento(request);

        assertEquals(
                HttpStatus.CREATED,
                resultado.getStatusCode()
        );

        assertEquals(
                respuesta,
                resultado.getBody()
        );

        verify(inventoryService)
                .registrarMovimiento(request);
    }

    @Test
    void listarMovimientosDevuelve200() {
        InventoryMovementResponseDTO movimiento =
                mock(InventoryMovementResponseDTO.class);

        when(inventoryService.listarMovimientos())
                .thenReturn(List.of(movimiento));

        ResponseEntity<List<InventoryMovementResponseDTO>> resultado =
                inventoryController.listarMovimientos();

        assertEquals(
                HttpStatus.OK,
                resultado.getStatusCode()
        );

        assertNotNull(resultado.getBody());
        assertEquals(1, resultado.getBody().size());

        verify(inventoryService)
                .listarMovimientos();
    }

    @Test
    void buscarMovimientoPorIdDevuelve200() {
        InventoryMovementResponseDTO movimiento =
                mock(InventoryMovementResponseDTO.class);

        when(inventoryService.buscarMovimientoPorId(10L))
                .thenReturn(movimiento);

        ResponseEntity<InventoryMovementResponseDTO> resultado =
                inventoryController.buscarMovimientoPorId(10L);

        assertEquals(
                HttpStatus.OK,
                resultado.getStatusCode()
        );

        assertEquals(
                movimiento,
                resultado.getBody()
        );

        verify(inventoryService)
                .buscarMovimientoPorId(10L);
    }

    @Test
    void eliminarMovimientoDevuelve204() {
        ResponseEntity<Void> resultado =
                inventoryController.eliminarMovimiento(10L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                resultado.getStatusCode()
        );

        verify(inventoryService)
                .eliminarMovimiento(10L);
    }
    @Test
void listarMovimientosPorLibroDevuelve200() {
    InventoryMovementResponseDTO movimiento =
            mock(InventoryMovementResponseDTO.class);

    when(inventoryService.listarMovimientosPorLibro(2L))
            .thenReturn(List.of(movimiento));

    ResponseEntity<List<InventoryMovementResponseDTO>> resultado =
            inventoryController.listarMovimientosPorLibro(2L);

    assertEquals(
            HttpStatus.OK,
            resultado.getStatusCode()
    );

    assertNotNull(resultado.getBody());
    assertEquals(1, resultado.getBody().size());

    verify(inventoryService)
            .listarMovimientosPorLibro(2L);
}

@Test
void listarMovimientosPorTipoDevuelve200() {
    InventoryMovementResponseDTO movimiento =
            mock(InventoryMovementResponseDTO.class);

    when(inventoryService.listarMovimientosPorTipo(
            TipoMovimiento.PERDIDO
    )).thenReturn(List.of(movimiento));

    ResponseEntity<List<InventoryMovementResponseDTO>> resultado =
            inventoryController.listarMovimientosPorTipo(
                    TipoMovimiento.PERDIDO
            );

    assertEquals(
            HttpStatus.OK,
            resultado.getStatusCode()
    );

    assertNotNull(resultado.getBody());
    assertEquals(1, resultado.getBody().size());

    verify(inventoryService)
            .listarMovimientosPorTipo(
                    TipoMovimiento.PERDIDO
            );
}

@Test
void obtenerResumenPorLibroDevuelve200() {
    InventorySummaryDTO resumen =
            mock(InventorySummaryDTO.class);

    when(inventoryService.obtenerResumenPorLibro(2L))
            .thenReturn(resumen);

    ResponseEntity<InventorySummaryDTO> resultado =
            inventoryController.obtenerResumenPorLibro(2L);

    assertEquals(
            HttpStatus.OK,
            resultado.getStatusCode()
    );

    assertEquals(
            resumen,
            resultado.getBody()
    );

    verify(inventoryService)
            .obtenerResumenPorLibro(2L);
}

@Test
void listarMovimientosPorLibroSinResultadosDevuelveListaVacia() {
    when(inventoryService.listarMovimientosPorLibro(3L))
            .thenReturn(List.of());

    ResponseEntity<List<InventoryMovementResponseDTO>> resultado =
            inventoryController.listarMovimientosPorLibro(3L);

    assertEquals(
            HttpStatus.OK,
            resultado.getStatusCode()
    );

    assertNotNull(resultado.getBody());
    assertEquals(0, resultado.getBody().size());

    verify(inventoryService)
            .listarMovimientosPorLibro(3L);
}
}