package com.library.copia_service.dto;

import com.library.copia_service.model.EstadoCopia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CopiaRequestDTO {

    @NotBlank(message = "El código de copia es obligatorio")
    @Size(max = 50, message = "El código de copia no puede superar 50 caracteres")
    @Pattern(
            regexp = "^COPY-[0-9]{3,}$",
            message = "El código debe tener un formato como COPY-001"
    )
    private String codigoCopia;

    @NotNull(message = "El identificador del libro es obligatorio")
    @Positive(message = "El identificador del libro debe ser mayor que cero")
    private Long bookId;

    private EstadoCopia estado;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 100, message = "La ubicación no puede superar 100 caracteres")
    private String ubicacion;

    @Size(max = 255, message = "La observación no puede superar 255 caracteres")
    private String observacion;
}