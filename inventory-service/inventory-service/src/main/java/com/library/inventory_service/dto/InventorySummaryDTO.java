package com.library.inventory_service.dto;

public class InventorySummaryDTO {

    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Long copiasDisponibles;
    private Long totalMovimientos;
    private Integer totalEntradas;
    private Integer totalSalidas;
    private Integer balanceMovimientos;

    public InventorySummaryDTO() {
    }

    public InventorySummaryDTO(Long bookId, String bookTitle, String bookAuthor, Long copiasDisponibles,
                               Long totalMovimientos, Integer totalEntradas, Integer totalSalidas,
                               Integer balanceMovimientos) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.copiasDisponibles = copiasDisponibles;
        this.totalMovimientos = totalMovimientos;
        this.totalEntradas = totalEntradas;
        this.totalSalidas = totalSalidas;
        this.balanceMovimientos = balanceMovimientos;
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

    public Long getCopiasDisponibles() {
        return copiasDisponibles;
    }

    public Long getTotalMovimientos() {
        return totalMovimientos;
    }

    public Integer getTotalEntradas() {
        return totalEntradas;
    }

    public Integer getTotalSalidas() {
        return totalSalidas;
    }

    public Integer getBalanceMovimientos() {
        return balanceMovimientos;
    }
}