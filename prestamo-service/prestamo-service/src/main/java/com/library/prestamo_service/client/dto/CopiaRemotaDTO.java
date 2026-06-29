package com.library.prestamo_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una copia recibida desde copia-service.
 *
 * No es una entidad JPA y no crea ninguna tabla
 * en la base de datos de prestamo-service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CopiaRemotaDTO {

    private Long id;

    private String codigoCopia;

    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    private EstadoCopiaRemota estado;

    private String ubicacion;

    private String observacion;

    private LocalDateTime fechaRegistro;
}