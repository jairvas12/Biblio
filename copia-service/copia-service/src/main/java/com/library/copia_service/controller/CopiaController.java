package com.library.copia_service.controller;

import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.service.CopiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/copias")
@Tag(
        name = "Copias",
        description = "Administración de las copias físicas de los libros"
)
public class CopiaController {

    private final CopiaService copiaService;

    @Operation(
            summary = "Crear una copia",
            description = "Registra una copia física asociada a un libro "
                    + "existente. La copia se crea inicialmente "
                    + "con estado AVAILABLE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Copia creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o código de copia duplicado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El libro asociado no existe"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Book-service no está disponible"
            )
    })
    @PostMapping
    public ResponseEntity<CopiaResponseDTO> crearCopia(
            @Valid
            @RequestBody
            CopiaRequestDTO requestDTO
    ) {
        log.info("Solicitud REST para crear copia");

        CopiaResponseDTO response =
                copiaService.crearCopia(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Listar todas las copias",
            description = "Obtiene todas las copias físicas registradas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copias obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            )
    })
    @GetMapping
    public ResponseEntity<List<CopiaResponseDTO>> listarCopias() {
        log.info("Solicitud REST para listar copias");

        return ResponseEntity.ok(
                copiaService.listarCopias()
        );
    }

    @Operation(
            summary = "Buscar una copia por ID",
            description = "Obtiene el detalle de una copia física específica."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copia encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador no es válido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La copia no existe"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CopiaResponseDTO> buscarCopiaPorId(
            @Parameter(
                    description = "Identificador de la copia",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador de la copia debe ser mayor que cero"
            )
            Long id
    ) {
        log.info(
                "Solicitud REST para buscar copia ID {}",
                id
        );

        return ResponseEntity.ok(
                copiaService.buscarCopiaPorId(id)
        );
    }

    @Operation(
            summary = "Listar copias de un libro",
            description = "Obtiene todas las copias asociadas "
                    + "al identificador de un libro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copias del libro obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador del libro no es válido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El libro no existe"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Book-service no está disponible"
            )
    })
    @GetMapping("/libro/{bookId}")
    public ResponseEntity<List<CopiaResponseDTO>> listarCopiasPorLibro(
            @Parameter(
                    description = "Identificador del libro",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador del libro debe ser mayor que cero"
            )
            Long bookId
    ) {
        log.info(
                "Solicitud REST para listar copias del libro ID {}",
                bookId
        );

        return ResponseEntity.ok(
                copiaService.listarCopiasPorLibro(bookId)
        );
    }

    @Operation(
            summary = "Listar copias disponibles de un libro",
            description = "Obtiene las copias de un libro "
                    + "que tienen estado AVAILABLE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copias disponibles obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador del libro no es válido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El libro no existe"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Book-service no está disponible"
            )
    })
    @GetMapping("/libro/{bookId}/disponibles")
    public ResponseEntity<List<CopiaResponseDTO>>
    listarCopiasDisponiblesPorLibro(
            @Parameter(
                    description = "Identificador del libro",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador del libro debe ser mayor que cero"
            )
            Long bookId
    ) {
        log.info(
                "Solicitud REST para listar copias disponibles "
                        + "del libro ID {}",
                bookId
        );

        return ResponseEntity.ok(
                copiaService.listarCopiasDisponiblesPorLibro(bookId)
        );
    }

    @Operation(
            summary = "Contar copias disponibles de un libro",
            description = "Obtiene la cantidad de copias "
                    + "con estado AVAILABLE para un libro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cantidad obtenida correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador del libro no es válido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El libro no existe"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Book-service no está disponible"
            )
    })
    @GetMapping("/libro/{bookId}/disponibles/cantidad")
    public ResponseEntity<Long> contarCopiasDisponiblesPorLibro(
            @Parameter(
                    description = "Identificador del libro",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador del libro debe ser mayor que cero"
            )
            Long bookId
    ) {
        log.info(
                "Solicitud REST para contar copias disponibles "
                        + "del libro ID {}",
                bookId
        );

        return ResponseEntity.ok(
                copiaService.contarCopiasDisponiblesPorLibro(bookId)
        );
    }

    @Operation(
            summary = "Listar copias por estado",
            description = "Filtra las copias usando un estado del dominio: "
                    + "AVAILABLE, LOANED, RESERVED, DAMAGED o LOST."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copias filtradas correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El estado enviado no es válido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para consultar copias"
            )
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CopiaResponseDTO>> listarCopiasPorEstado(
            @Parameter(
                    description = "Estado de las copias",
                    example = "AVAILABLE"
            )
            @PathVariable
            EstadoCopia estado
    ) {
        log.info(
                "Solicitud REST para listar copias por estado {}",
                estado
        );

        return ResponseEntity.ok(
                copiaService.listarCopiasPorEstado(estado)
        );
    }

    @Operation(
            summary = "Actualizar una copia",
            description = "Actualiza el código, libro, ubicación "
                    + "y observación de una copia. "
                    + "El estado debe modificarse mediante el endpoint PATCH."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Copia actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, código duplicado "
                            + "o intento de modificar directamente el estado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para actualizar copias"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La copia o el libro no existen"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Book-service no está disponible"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CopiaResponseDTO> actualizarCopia(
            @Parameter(
                    description = "Identificador de la copia",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador de la copia debe ser mayor que cero"
            )
            Long id,

            @Valid
            @RequestBody
            CopiaRequestDTO requestDTO
    ) {
        log.info(
                "Solicitud REST para actualizar copia ID {}",
                id
        );

        return ResponseEntity.ok(
                copiaService.actualizarCopia(
                        id,
                        requestDTO
                )
        );
    }

    @Operation(
            summary = "Cambiar el estado de una copia",
            description = "Realiza una transición controlada "
                    + "entre los estados de una copia."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido o transición no permitida"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para cambiar estados"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La copia no existe"
            )
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<CopiaResponseDTO> cambiarEstado(
            @Parameter(
                    description = "Identificador de la copia",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador de la copia debe ser mayor que cero"
            )
            Long id,

            @Parameter(
                    description = "Nuevo estado de la copia",
                    example = "LOANED"
            )
            @RequestParam
            @NotNull(
                    message = "El nuevo estado de la copia es obligatorio"
            )
            EstadoCopia estado
    ) {
        log.info(
                "Solicitud REST para cambiar estado de copia ID {} a {}",
                id,
                estado
        );

        return ResponseEntity.ok(
                copiaService.cambiarEstado(
                        id,
                        estado
                )
        );
    }

    @Operation(
            summary = "Eliminar una copia",
            description = "Elimina una copia siempre que no esté "
                    + "prestada ni reservada. Esta operación requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Copia eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La copia no puede eliminarse por su estado actual"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La copia no existe"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCopia(
            @Parameter(
                    description = "Identificador de la copia",
                    example = "1"
            )
            @PathVariable
            @Positive(
                    message = "El identificador de la copia debe ser mayor que cero"
            )
            Long id
    ) {
        log.info(
                "Solicitud REST para eliminar copia ID {}",
                id
        );

        copiaService.eliminarCopia(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}