package com.library.find_service.controller;

import com.library.find_service.dto.BookDTO;
import com.library.find_service.service.FindService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/find")
@Tag(
        name = "Búsqueda de libros",
        description = "Operaciones para consultar y buscar libros del catálogo"
)
@SecurityRequirement(
        name = "bearerAuth"
)
public class FindController {

    private final FindService findService;

    public FindController(
            FindService findService
    ) {
        this.findService =
                findService;
    }

    @Operation(
            summary = "Listar todos los libros",
            description = "Obtiene el catálogo completo desde BOOK Service"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo obtenido correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido o vencido"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "BOOK Service no está disponible"
            )
    })
    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>>
    getAllBooks() {

        return ResponseEntity.ok(
                findService.getAllBooks()
        );
    }

    @Operation(
            summary = "Buscar libros por título",
            description = "Busca coincidencias parciales sin distinguir mayúsculas y minúsculas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El título está vacío o no fue informado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido o vencido"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "BOOK Service no está disponible"
            )
    })
    @GetMapping("/title")
    public ResponseEntity<List<BookDTO>>
    findByTitle(
            @Parameter(
                    description = "Título completo o parcial del libro",
                    example = "Historia",
                    required = true
            )
            @RequestParam
            String title
    ) {
        return ResponseEntity.ok(
                findService.findByTitle(
                        title
                )
        );
    }

    @Operation(
            summary = "Buscar libros por autor",
            description = "Busca coincidencias parciales sin distinguir mayúsculas y minúsculas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El autor está vacío o no fue informado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido o vencido"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "BOOK Service no está disponible"
            )
    })
    @GetMapping("/author")
    public ResponseEntity<List<BookDTO>>
    findByAuthor(
            @Parameter(
                    description = "Nombre completo o parcial del autor",
                    example = "Isabel Allende",
                    required = true
            )
            @RequestParam
            String author
    ) {
        return ResponseEntity.ok(
                findService.findByAuthor(
                        author
                )
        );
    }

    @Operation(
            summary = "Buscar libros por categoría",
            description = "Busca coincidencias parciales en el nombre de la categoría"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La categoría está vacía o no fue informada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido o vencido"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "BOOK Service no está disponible"
            )
    })
    @GetMapping("/category")
    public ResponseEntity<List<BookDTO>>
    findByCategory(
            @Parameter(
                    description = "Nombre completo o parcial de la categoría",
                    example = "Novela",
                    required = true
            )
            @RequestParam
            String category
    ) {
        return ResponseEntity.ok(
                findService.findByCategory(
                        category
                )
        );
    }
}