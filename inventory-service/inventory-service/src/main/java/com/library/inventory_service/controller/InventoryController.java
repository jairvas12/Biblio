package com.library.inventory_service.controller;

import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.dto.InventoryMovementResponseDTO;
import com.library.inventory_service.dto.InventorySummaryDTO;
import com.library.inventory_service.model.TipoMovimiento;
import com.library.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/movements")
    public ResponseEntity<InventoryMovementResponseDTO> registrarMovimiento(
            @Valid @RequestBody InventoryMovementRequestDTO requestDTO) {

        log.info("Solicitud REST para registrar movimiento de inventario");

        InventoryMovementResponseDTO response = inventoryService.registrarMovimiento(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/movements")
    public ResponseEntity<List<InventoryMovementResponseDTO>> listarMovimientos() {
        log.info("Solicitud REST para listar movimientos");

        return ResponseEntity.ok(inventoryService.listarMovimientos());
    }

    @GetMapping("/movements/{id}")
    public ResponseEntity<InventoryMovementResponseDTO> buscarMovimientoPorId(@PathVariable Long id) {
        log.info("Solicitud REST para buscar movimiento ID {}", id);

        return ResponseEntity.ok(inventoryService.buscarMovimientoPorId(id));
    }

    @GetMapping("/book/{bookId}/movements")
    public ResponseEntity<List<InventoryMovementResponseDTO>> listarMovimientosPorLibro(@PathVariable Long bookId) {
        log.info("Solicitud REST para listar movimientos del libro ID {}", bookId);

        return ResponseEntity.ok(inventoryService.listarMovimientosPorLibro(bookId));
    }

    @GetMapping("/type/{tipoMovimiento}")
    public ResponseEntity<List<InventoryMovementResponseDTO>> listarMovimientosPorTipo(
            @PathVariable TipoMovimiento tipoMovimiento) {

        log.info("Solicitud REST para listar movimientos por tipo {}", tipoMovimiento);

        return ResponseEntity.ok(inventoryService.listarMovimientosPorTipo(tipoMovimiento));
    }

    @GetMapping("/book/{bookId}/summary")
    public ResponseEntity<InventorySummaryDTO> obtenerResumenPorLibro(@PathVariable Long bookId) {
        log.info("Solicitud REST para obtener resumen de inventario del libro ID {}", bookId);

        return ResponseEntity.ok(inventoryService.obtenerResumenPorLibro(bookId));
    }

    @DeleteMapping("/movements/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        log.info("Solicitud REST para eliminar movimiento ID {}", id);

        inventoryService.eliminarMovimiento(id);

        return ResponseEntity.noContent().build();
    }
}