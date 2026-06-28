package com.library.category_service.controller;

import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(
        name = "Categorías",
        description = "Operaciones para gestionar categorías de libros"
)
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(
            summary = "Crear una categoría",
            description = "Requiere rol ADMIN o BIBLIOTECARIO"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o categoría duplicada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            )
    })
    public ResponseEntity<CategoryResponseDTO> saveCategory(
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO response =
                categoryService.saveCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Requiere rol ADMIN, BIBLIOTECARIO o USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            )
    })
    public ResponseEntity<List<CategoryResponseDTO>>
    getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar una categoría por ID",
            description = "Requiere rol ADMIN, BIBLIOTECARIO o USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<CategoryResponseDTO>
    getCategoryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una categoría",
            description = "Requiere rol ADMIN o BIBLIOTECARIO"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o categoría duplicada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<CategoryResponseDTO>
    updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        return ResponseEntity.ok(
                categoryService.updateCategory(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una categoría",
            description = "Requiere rol ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}