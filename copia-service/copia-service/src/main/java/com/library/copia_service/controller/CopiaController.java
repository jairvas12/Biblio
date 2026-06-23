package com.library.copia_service.controller;

import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.service.CopiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/copias")
public class CopiaController {

    private final CopiaService copiaService;

    @PostMapping
    public ResponseEntity<CopiaResponseDTO> crearCopia(@Valid @RequestBody CopiaRequestDTO requestDTO) {
        log.info("Solicitud REST para crear copia");

        CopiaResponseDTO response = copiaService.crearCopia(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CopiaResponseDTO>> listarCopias() {
        log.info("Solicitud REST para listar copias");

        return ResponseEntity.ok(copiaService.listarCopias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CopiaResponseDTO> buscarCopiaPorId(@PathVariable Long id) {
        log.info("Solicitud REST para buscar copia ID {}", id);

        return ResponseEntity.ok(copiaService.buscarCopiaPorId(id));
    }

    @GetMapping("/libro/{bookId}")
    public ResponseEntity<List<CopiaResponseDTO>> listarCopiasPorLibro(@PathVariable Long bookId) {
        log.info("Solicitud REST para listar copias del libro ID {}", bookId);

        return ResponseEntity.ok(copiaService.listarCopiasPorLibro(bookId));
    }

    @GetMapping("/libro/{bookId}/disponibles")
    public ResponseEntity<List<CopiaResponseDTO>> listarCopiasDisponiblesPorLibro(@PathVariable Long bookId) {
        log.info("Solicitud REST para listar copias disponibles del libro ID {}", bookId);

        return ResponseEntity.ok(copiaService.listarCopiasDisponiblesPorLibro(bookId));
    }

    @GetMapping("/libro/{bookId}/disponibles/cantidad")
    public ResponseEntity<Long> contarCopiasDisponiblesPorLibro(@PathVariable Long bookId) {
        log.info("Solicitud REST para contar copias disponibles del libro ID {}", bookId);

        return ResponseEntity.ok(copiaService.contarCopiasDisponiblesPorLibro(bookId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CopiaResponseDTO>> listarCopiasPorEstado(@PathVariable EstadoCopia estado) {
        log.info("Solicitud REST para listar copias por estado {}", estado);

        return ResponseEntity.ok(copiaService.listarCopiasPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CopiaResponseDTO> actualizarCopia(
            @PathVariable Long id,
            @Valid @RequestBody CopiaRequestDTO requestDTO
    ) {
        log.info("Solicitud REST para actualizar copia ID {}", id);

        return ResponseEntity.ok(copiaService.actualizarCopia(id, requestDTO));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CopiaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoCopia estado
    ) {
        log.info("Solicitud REST para cambiar estado de copia ID {}", id);

        return ResponseEntity.ok(copiaService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCopia(@PathVariable Long id) {
        log.info("Solicitud REST para eliminar copia ID {}", id);

        copiaService.eliminarCopia(id);

        return ResponseEntity.noContent().build();
    }
}
