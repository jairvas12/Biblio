package com.library.book_service.controller;

import com.library.book_service.dto.BookRequestDTO;
import com.library.book_service.dto.BookResponseDTO;

import com.library.book_service.exception.ApiErrorResponse;

import com.library.book_service.service.BookService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@Tag(
        name = "Books",
        description = "Administración del catálogo bibliográfico"
)
@SecurityRequirement(
        name = "bearerAuth"
)
public class BookController {

    private final BookService bookService;

    public BookController(
            BookService bookService
    ) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(
            summary = "Crear un libro",
            description = """
                    Registra un libro nuevo, valida que el ISBN no esté duplicado
                    y comprueba remotamente que la categoría exista.
                    Requiere rol ADMIN o BIBLIOTECARIO.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Libro creado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, ISBN duplicado o categoría inexistente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inexistente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no tiene permiso para crear libros"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "CATEGORY no está disponible",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<BookResponseDTO> createBook(

            @RequestBody(
                    required = true,
                    description = "Información del libro que se registrará"
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            BookRequestDTO request
    ) {
        BookResponseDTO response =
                bookService.createBook(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los libros",
            description = """
                    Devuelve el catálogo ordenado por título.
                    El nombre de cada categoría se obtiene desde CATEGORY.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado recuperado correctamente",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = BookResponseDTO.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inexistente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no tiene permiso"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "CATEGORY no está disponible",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<List<BookResponseDTO>>
    getAllBooks() {

        return ResponseEntity.ok(
                bookService.getAllBooks()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un libro por ID",
            description = "Busca un libro y obtiene el nombre de su categoría desde CATEGORY"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Libro encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inexistente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no tiene permiso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Libro inexistente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "CATEGORY no está disponible",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<BookResponseDTO>
    getBookById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                bookService.getBookById(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un libro",
            description = """
                    Actualiza los datos de un libro existente.
                    Requiere rol ADMIN o BIBLIOTECARIO.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Libro actualizado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, ISBN duplicado o categoría inexistente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inexistente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no tiene permiso para actualizar"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Libro inexistente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "CATEGORY no está disponible",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<BookResponseDTO>
    updateBook(
            @PathVariable Long id,

            @RequestBody(
                    required = true,
                    description = "Nuevos datos del libro"
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            BookRequestDTO request
    ) {
        return ResponseEntity.ok(
                bookService.updateBook(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un libro",
            description = "Elimina un libro existente. Requiere rol ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Libro eliminado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inexistente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no tiene permiso para eliminar"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Libro inexistente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id
    ) {
        bookService.deleteBook(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}