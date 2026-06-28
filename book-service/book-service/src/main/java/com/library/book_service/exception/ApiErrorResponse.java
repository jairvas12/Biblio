package com.library.book_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Respuesta uniforme para errores de la API"
)
public class ApiErrorResponse {

    @Schema(
            description = "Fecha y hora del error",
            example = "2026-06-28T18:00:00"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "Código HTTP",
            example = "400"
    )
    private int status;

    @Schema(
            description = "Tipo de error",
            example = "VALIDATION ERROR"
    )
    private String error;

    @Schema(
            description = "Descripción del problema",
            example = "Existen datos inválidos en la solicitud"
    )
    private String message;

    @Schema(
            description = "Ruta solicitada",
            example = "/books"
    )
    private String path;

    @Schema(
            description = "Errores específicos por campo"
    )
    private Map<String, String> validationErrors;
}