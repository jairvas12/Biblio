package com.library.prestamo_service.controller;

import com.library.prestamo_service.dto.ActualizacionAtrasosResponseDTO;
import com.library.prestamo_service.dto.CrearPrestamoRequestDTO;
import com.library.prestamo_service.dto.DevolverPrestamoRequestDTO;
import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/prestamos")
@Tag(
name = "Préstamos",
description = "Gestión de préstamos y devoluciones de copias"
)
public class PrestamoController {


private final PrestamoService prestamoService;

@Operation(
        summary = "Crear un préstamo",
        description = "Valida la disponibilidad de la copia, "
                + "el límite de préstamos del usuario y registra "
                + "un préstamo nuevo."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Préstamo creado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "La copia solicitada no existe"
        ),
        @ApiResponse(
                responseCode = "409",
                description = "La copia no está disponible o "
                        + "se incumple una regla de negocio"
        ),
        @ApiResponse(
                responseCode = "503",
                description = "No fue posible comunicarse con copia-service"
        )
})
@PostMapping
public ResponseEntity<PrestamoResponseDTO> crearPrestamo(
        @Valid
        @RequestBody
        CrearPrestamoRequestDTO requestDTO
) {
    PrestamoResponseDTO prestamoCreado =
            prestamoService.crearPrestamo(
                    requestDTO
            );

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(prestamoCreado);
}

@Operation(
        summary = "Listar todos los préstamos",
        description = "Obtiene todos los préstamos registrados."
)
@ApiResponse(
        responseCode = "200",
        description = "Préstamos obtenidos correctamente"
)
@GetMapping
public ResponseEntity<List<PrestamoResponseDTO>> listarTodos() {
    return ResponseEntity.ok(
            prestamoService.listarTodos()
    );
}

@Operation(
        summary = "Buscar un préstamo por ID",
        description = "Obtiene el detalle de un préstamo específico."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Préstamo encontrado"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Identificador inválido"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Préstamo no encontrado"
        )
})
@GetMapping("/{prestamoId}")
public ResponseEntity<PrestamoResponseDTO> obtenerPorId(
        @PathVariable
        @Positive(
                message = "El identificador del préstamo "
                        + "debe ser mayor que cero"
        )
        Long prestamoId
) {
    return ResponseEntity.ok(
            prestamoService.obtenerPorId(
                    prestamoId
            )
    );
}

@Operation(
        summary = "Listar préstamos por usuario",
        description = "Obtiene el historial de préstamos "
                + "asociado a un usuario."
)
@ApiResponse(
        responseCode = "200",
        description = "Historial del usuario obtenido correctamente"
)
@GetMapping("/usuario/{usuarioId}")
public ResponseEntity<List<PrestamoResponseDTO>> listarPorUsuario(
        @PathVariable
        @Positive(
                message = "El identificador del usuario "
                        + "debe ser mayor que cero"
        )
        Long usuarioId
) {
    return ResponseEntity.ok(
            prestamoService.listarPorUsuario(
                    usuarioId
            )
    );
}

@Operation(
        summary = "Listar préstamos por copia",
        description = "Obtiene el historial de préstamos "
                + "asociado a una copia física."
)
@ApiResponse(
        responseCode = "200",
        description = "Historial de la copia obtenido correctamente"
)
@GetMapping("/copia/{copiaId}")
public ResponseEntity<List<PrestamoResponseDTO>> listarPorCopia(
        @PathVariable
        @Positive(
                message = "El identificador de la copia "
                        + "debe ser mayor que cero"
        )
        Long copiaId
) {
    return ResponseEntity.ok(
            prestamoService.listarPorCopia(
                    copiaId
            )
    );
}

@Operation(
        summary = "Listar préstamos por estado",
        description = "Filtra los préstamos por ACTIVO, ATRASADO, "
                + "DEVUELTO o CANCELADO."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Préstamos filtrados correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "El estado enviado no es válido"
        )
})
@GetMapping("/estado/{estado}")
public ResponseEntity<List<PrestamoResponseDTO>> listarPorEstado(
        @PathVariable
        EstadoPrestamo estado
) {
    return ResponseEntity.ok(
            prestamoService.listarPorEstado(
                    estado
            )
    );
}

@Operation(
        summary = "Registrar devolución",
        description = "Registra la devolución del préstamo "
                + "y cambia la copia de LOANED a AVAILABLE."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Devolución registrada correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Préstamo o copia no encontrados"
        ),
        @ApiResponse(
                responseCode = "409",
                description = "El préstamo ya fue devuelto, "
                        + "fue cancelado o existe una inconsistencia"
        ),
        @ApiResponse(
                responseCode = "503",
                description = "No fue posible comunicarse con copia-service"
        )
})
@PatchMapping("/{prestamoId}/devolucion")
public ResponseEntity<PrestamoResponseDTO> devolverPrestamo(
        @PathVariable
        @Positive(
                message = "El identificador del préstamo "
                        + "debe ser mayor que cero"
        )
        Long prestamoId,

        @Valid
        @RequestBody(required = false)
        DevolverPrestamoRequestDTO requestDTO
) {
    return ResponseEntity.ok(
            prestamoService.devolverPrestamo(
                    prestamoId,
                    requestDTO
            )
    );
}

@Operation(
        summary = "Actualizar préstamos atrasados",
        description = "Busca préstamos ACTIVO cuya fecha de vencimiento "
                + "ya pasó y los cambia a ATRASADO."
)
@ApiResponse(
        responseCode = "200",
        description = "Actualización completada correctamente"
)
@PatchMapping("/actualizar-atrasados")
public ResponseEntity<ActualizacionAtrasosResponseDTO>
actualizarPrestamosAtrasados() {

    int cantidadActualizada =
            prestamoService.actualizarPrestamosAtrasados();

    ActualizacionAtrasosResponseDTO respuesta =
            new ActualizacionAtrasosResponseDTO(
                    cantidadActualizada,
                    "Actualización de préstamos atrasados completada"
            );

    return ResponseEntity.ok(
            respuesta
    );
}

}
