package com.library.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
        description = "Datos requeridos para crear o actualizar un libro"
)
public class BookRequestDTO {

    @Schema(
            description = "Código ISBN del libro",
            example = "9789560000011"
    )
    @NotBlank(
            message = "El ISBN es obligatorio"
    )
    @Size(
            min = 10,
            max = 20,
            message = "El ISBN debe tener entre 10 y 20 caracteres"
    )
    private String isbn;

    @Schema(
            description = "Título del libro",
            example = "Historia de Chile"
    )
    @NotBlank(
            message = "El título es obligatorio"
    )
    @Size(
            min = 2,
            max = 200,
            message = "El título debe tener entre 2 y 200 caracteres"
    )
    private String title;

    @Schema(
            description = "Autor del libro",
            example = "Francisco Antonio Encina"
    )
    @NotBlank(
            message = "El autor es obligatorio"
    )
    @Size(
            min = 2,
            max = 150,
            message = "El autor debe tener entre 2 y 150 caracteres"
    )
    private String author;

    @Schema(
            description = "Año de publicación",
            example = "2020"
    )
    @NotNull(
            message = "El año de publicación es obligatorio"
    )
    @Min(
            value = 1000,
            message = "El año de publicación debe ser igual o superior a 1000"
    )
    private Integer publicationYear;

    @Schema(
            description = "Identificador de la categoría registrada en CATEGORY",
            example = "1"
    )
    @NotNull(
            message = "La categoría es obligatoria"
    )
    @Positive(
            message = "El identificador de categoría debe ser mayor que cero"
    )
    private Long categoryId;
}