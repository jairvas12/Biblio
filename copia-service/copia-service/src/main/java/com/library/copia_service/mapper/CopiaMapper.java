package com.library.copia_service.mapper;

import com.library.copia_service.dto.BookResponseDTO;
import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.model.Copia;
import com.library.copia_service.model.EstadoCopia;
import org.springframework.stereotype.Component;

@Component
public class CopiaMapper {

    public Copia toEntity(CopiaRequestDTO requestDTO) {
        return Copia.builder()
                .codigoCopia(requestDTO.getCodigoCopia())
                .bookId(requestDTO.getBookId())
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion(requestDTO.getUbicacion())
                .observacion(requestDTO.getObservacion())
                .build();
    }

    public void updateEntity(Copia copia, CopiaRequestDTO requestDTO) {
        copia.setCodigoCopia(requestDTO.getCodigoCopia());
        copia.setBookId(requestDTO.getBookId());
        copia.setUbicacion(requestDTO.getUbicacion());
        copia.setObservacion(requestDTO.getObservacion());
    }

    public CopiaResponseDTO toResponseDTO(
            Copia copia,
            BookResponseDTO libro
    ) {
        return CopiaResponseDTO.builder()
                .id(copia.getId())
                .codigoCopia(copia.getCodigoCopia())
                .bookId(copia.getBookId())
                .bookTitle(libro != null ? libro.getTitle() : null)
                .bookAuthor(libro != null ? libro.getAuthor() : null)
                .estado(copia.getEstado())
                .ubicacion(copia.getUbicacion())
                .observacion(copia.getObservacion())
                .fechaRegistro(copia.getFechaRegistro())
                .build();
    }
}