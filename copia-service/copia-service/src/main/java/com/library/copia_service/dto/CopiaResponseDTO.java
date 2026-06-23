package com.library.copia_service.dto;

import com.library.copia_service.model.EstadoCopia;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopiaResponseDTO {

    private Long id;
    private String codigoCopia;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private EstadoCopia estado;
    private String ubicacion;
    private String observacion;
    private LocalDateTime fechaRegistro;
}