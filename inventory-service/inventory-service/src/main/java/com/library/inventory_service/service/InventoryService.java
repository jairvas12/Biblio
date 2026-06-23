package com.library.inventory_service.service;

import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.dto.InventoryMovementResponseDTO;
import com.library.inventory_service.dto.InventorySummaryDTO;
import com.library.inventory_service.model.TipoMovimiento;

import java.util.List;

public interface InventoryService {

    InventoryMovementResponseDTO registrarMovimiento(InventoryMovementRequestDTO requestDTO);

    List<InventoryMovementResponseDTO> listarMovimientos();

    InventoryMovementResponseDTO buscarMovimientoPorId(Long id);

    List<InventoryMovementResponseDTO> listarMovimientosPorLibro(Long bookId);

    List<InventoryMovementResponseDTO> listarMovimientosPorTipo(TipoMovimiento tipoMovimiento);

    InventorySummaryDTO obtenerResumenPorLibro(Long bookId);

    void eliminarMovimiento(Long id);
}