package com.library.find_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private Long id;

    private String isbn;

    private String title;

    private String author;

    private Integer publicationYear;

    private Long categoryId;

    private String category;
}