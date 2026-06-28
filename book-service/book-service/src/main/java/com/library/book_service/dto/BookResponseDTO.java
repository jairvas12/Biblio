package com.library.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Información pública de un libro"
)
public class BookResponseDTO {

    @Schema(
            description = "Identificador interno del libro",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Código ISBN normalizado",
            example = "9789560000011"
    )
    private String isbn;

    @Schema(
            description = "Título del libro",
            example = "Historia de Chile"
    )
    private String title;

    @Schema(
            description = "Autor del libro",
            example = "Francisco Antonio Encina"
    )
    private String author;

    @Schema(
            description = "Año de publicación",
            example = "2020"
    )
    private Integer publicationYear;

    @Schema(
            description = "Identificador de la categoría",
            example = "1"
    )
    private Long categoryId;

    @Schema(
            description = "Nombre obtenido remotamente desde CATEGORY",
            example = "Historia"
    )
    private String category;
}