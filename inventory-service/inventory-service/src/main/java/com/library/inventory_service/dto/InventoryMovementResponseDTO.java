package com.library.inventory_service.dto;

import com.library.inventory_service.model.TipoMovimiento;

import java.time.LocalDateTime;

public class InventoryMovementResponseDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private String motivo;
    private String responsable;
    private Long copiasDisponibles;
    private LocalDateTime fechaMovimiento;

    public InventoryMovementResponseDTO() {
    }

    public InventoryMovementResponseDTO(Long id, Long bookId, String bookTitle, String bookAuthor,
                                        TipoMovimiento tipoMovimiento, Integer cantidad, String motivo,
                                        String responsable, Long copiasDisponibles, LocalDateTime fechaMovimiento) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.responsable = responsable;
        this.copiasDisponibles = copiasDisponibles;
        this.fechaMovimiento = fechaMovimiento;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getResponsable() {
        return responsable;
    }

    public Long getCopiasDisponibles() {
        return copiasDisponibles;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }
}