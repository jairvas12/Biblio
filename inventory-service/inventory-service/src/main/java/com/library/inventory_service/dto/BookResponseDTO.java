package com.library.inventory_service.dto;

public class BookResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String category;
    private Integer stock;

    public BookResponseDTO() {
    }

    public BookResponseDTO(Long id, String title, String author, String category, Integer stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public Integer getStock() {
        return stock;
    }
}