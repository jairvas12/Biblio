package com.library.inventory_service.repository;

import com.library.inventory_service.model.InventoryMovement;
import com.library.inventory_service.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByBookId(Long bookId);

    List<InventoryMovement> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    long countByBookId(Long bookId);
}