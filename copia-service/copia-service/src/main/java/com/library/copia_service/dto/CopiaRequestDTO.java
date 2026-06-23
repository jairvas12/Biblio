package com.library.copia_service.dto;

import com.library.copia_service.model.EstadoCopia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CopiaRequestDTO {

    @NotBlank(message = "El código de copia es obligatorio")
    @Size(max = 50, message = "El código de copia no puede superar 50 caracteres")
    @Pattern(regexp = "^COPY-[0-9]{3,}$", message = "El código debe tener formato COPY-001")
    private String codigoCopia;

    @NotNull(message = "El ID del libro es obligatorio")
    @Positive(message = "El ID del libro debe ser positivo")
    private Long bookId;

    private EstadoCopia estado;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 100, message = "La ubicación no puede superar 100 caracteres")
    private String ubicacion;

    @Size(max = 255, message = "La observación no puede superar 255 caracteres")
    private String observacion;

    public CopiaRequestDTO() {
    }

    public CopiaRequestDTO(String codigoCopia, Long bookId, EstadoCopia estado, String ubicacion, String observacion) {
        this.codigoCopia = codigoCopia;
        this.bookId = bookId;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
    }

    public String getCodigoCopia() {
        return codigoCopia;
    }

    public void setCodigoCopia(String codigoCopia) {
        this.codigoCopia = codigoCopia;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public EstadoCopia getEstado() {
        return estado;
    }

    public void setEstado(EstadoCopia estado) {
        this.estado = estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}