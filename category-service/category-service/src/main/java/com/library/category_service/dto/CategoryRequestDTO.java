package com.library.category_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequestDTO {

    @NotBlank(
            message = "El nombre de la categoría es obligatorio"
    )
    @Size(
            min = 2,
            max = 100,
            message = "El nombre de la categoría debe tener entre 2 y 100 caracteres"
    )
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
