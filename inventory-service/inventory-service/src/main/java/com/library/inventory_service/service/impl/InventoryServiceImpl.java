package com.library.inventory_service.service.impl;

import com.library.inventory_service.client.BookClient;
import com.library.inventory_service.client.CopiaClient;
import com.library.inventory_service.dto.BookResponseDTO;
import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.dto.InventoryMovementResponseDTO;
import com.library.inventory_service.dto.InventorySummaryDTO;
import com.library.inventory_service.exception.BusinessException;
import com.library.inventory_service.exception.RemoteServiceException;
import com.library.inventory_service.exception.ResourceNotFoundException;
import com.library.inventory_service.model.InventoryMovement;
import com.library.inventory_service.model.TipoMovimiento;
import com.library.inventory_service.repository.InventoryMovementRepository;
import com.library.inventory_service.service.InventoryService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryMovementRepository inventoryMovementRepository;
    private final BookClient bookClient;
    private final CopiaClient copiaClient;

    public InventoryServiceImpl(
            InventoryMovementRepository inventoryMovementRepository,
            BookClient bookClient,
            CopiaClient copiaClient) {

        this.inventoryMovementRepository = inventoryMovementRepository;
        this.bookClient = bookClient;
        this.copiaClient = copiaClient;
    }

    @Override
    public InventoryMovementResponseDTO registrarMovimiento(
            InventoryMovementRequestDTO requestDTO) {

        if (requestDTO == null) {
            throw new BusinessException(
                    "La solicitud de movimiento no puede ser nula"
            );
        }

        log.info(
                "Registrando movimiento de inventario para libro ID {}",
                requestDTO.getBookId()
        );

        validarLibroExiste(requestDTO.getBookId());

        if (requiereDisponibilidad(requestDTO.getTipoMovimiento())) {

            Long disponibles =
                    obtenerCopiasDisponibles(requestDTO.getBookId());

            if (disponibles < requestDTO.getCantidad()) {
                throw new BusinessException(
                        "No hay suficientes copias disponibles para registrar este movimiento"
                );
            }
        }

        InventoryMovement movement =
                new InventoryMovement();

        movement.setBookId(
                requestDTO.getBookId()
        );

        movement.setTipoMovimiento(
                requestDTO.getTipoMovimiento()
        );

        movement.setCantidad(
                requestDTO.getCantidad()
        );

        movement.setMotivo(
                requestDTO.getMotivo()
        );

        movement.setResponsable(
                requestDTO.getResponsable()
        );

        InventoryMovement saved =
                inventoryMovementRepository.save(movement);

        log.info(
                "Movimiento de inventario creado correctamente con ID {}",
                saved.getId()
        );

        return convertirAResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> listarMovimientos() {

        log.info(
                "Listando todos los movimientos de inventario"
        );

        return inventoryMovementRepository
                .findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryMovementResponseDTO buscarMovimientoPorId(
            Long id) {

        log.info(
                "Buscando movimiento de inventario ID {}",
                id
        );

        InventoryMovement movement =
                obtenerMovimientoPorId(id);

        return convertirAResponseDTO(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> listarMovimientosPorLibro(
            Long bookId) {

        log.info(
                "Listando movimientos del libro ID {}",
                bookId
        );

        validarLibroExiste(bookId);

        return inventoryMovementRepository
                .findByBookId(bookId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> listarMovimientosPorTipo(
            TipoMovimiento tipoMovimiento) {

        log.info(
                "Listando movimientos por tipo {}",
                tipoMovimiento
        );

        return inventoryMovementRepository
                .findByTipoMovimiento(tipoMovimiento)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySummaryDTO obtenerResumenPorLibro(
            Long bookId) {

        log.info(
                "Generando resumen de inventario para libro ID {}",
                bookId
        );

        BookResponseDTO libro =
                validarLibroExiste(bookId);

        Long copiasDisponibles =
                obtenerCopiasDisponibles(bookId);

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByBookId(bookId);

        int totalEntradas = 0;
        int totalSalidas = 0;

        for (InventoryMovement movement : movements) {

            if (movement.getTipoMovimiento()
                    == TipoMovimiento.ENTRADA) {

                totalEntradas += movement.getCantidad();
            }

            if (movement.getTipoMovimiento()
                    == TipoMovimiento.SALIDA
                    || movement.getTipoMovimiento()
                    == TipoMovimiento.DANADO
                    || movement.getTipoMovimiento()
                    == TipoMovimiento.PERDIDO) {

                totalSalidas += movement.getCantidad();
            }
        }

        int balance =
                totalEntradas - totalSalidas;

        return new InventorySummaryDTO(
                bookId,
                libro.getTitle(),
                libro.getAuthor(),
                copiasDisponibles,
                (long) movements.size(),
                totalEntradas,
                totalSalidas,
                balance
        );
    }

    @Override
    public void eliminarMovimiento(Long id) {

        log.info(
                "Eliminando movimiento de inventario ID {}",
                id
        );

        InventoryMovement movement =
                obtenerMovimientoPorId(id);

        inventoryMovementRepository.delete(movement);

        log.info(
                "Movimiento eliminado correctamente ID {}",
                id
        );
    }

    private InventoryMovement obtenerMovimientoPorId(
            Long id) {

        return inventoryMovementRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe movimiento de inventario con ID: "
                                        + id
                        )
                );
    }

    private BookResponseDTO validarLibroExiste(
            Long bookId) {

        BookResponseDTO libro;

        try {
            libro =
                    bookClient.buscarLibroPorId(bookId);

        } catch (FeignException ex) {

            if (ex.status() == 404) {
                throw new ResourceNotFoundException(
                        "No existe libro en book-service con ID: "
                                + bookId
                );
            }

            log.error(
                    "Error al comunicarse con book-service. Status: {}",
                    ex.status()
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con book-service"
            );

        } catch (RuntimeException ex) {

            log.error(
                    "Error inesperado al comunicarse con book-service",
                    ex
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con book-service"
            );
        }

        if (libro == null) {
            throw new RemoteServiceException(
                    "book-service devolvió una respuesta vacía"
            );
        }

        return libro;
    }

    private Long obtenerCopiasDisponibles(
            Long bookId) {

        Long disponibles;

        try {
            disponibles =
                    copiaClient.contarCopiasDisponiblesPorLibro(
                            bookId
                    );

        } catch (FeignException ex) {

            log.error(
                    "Error al comunicarse con copia-service. Status: {}",
                    ex.status()
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con copia-service"
            );

        } catch (RuntimeException ex) {

            log.error(
                    "Error inesperado al comunicarse con copia-service",
                    ex
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con copia-service"
            );
        }

        if (disponibles == null) {
            throw new RemoteServiceException(
                    "copia-service devolvió una respuesta vacía"
            );
        }

        return disponibles;
    }

    private boolean requiereDisponibilidad(
            TipoMovimiento tipoMovimiento) {

        return tipoMovimiento == TipoMovimiento.SALIDA
                || tipoMovimiento == TipoMovimiento.DANADO
                || tipoMovimiento == TipoMovimiento.PERDIDO;
    }

    private InventoryMovementResponseDTO convertirAResponseDTO(
            InventoryMovement movement) {

        BookResponseDTO libro =
                obtenerLibroParaRespuesta(
                        movement.getBookId()
                );

        Long disponibles =
                obtenerDisponibilidadParaRespuesta(
                        movement.getBookId()
                );

        String title =
                libro != null
                        ? libro.getTitle()
                        : null;

        String author =
                libro != null
                        ? libro.getAuthor()
                        : null;

        return new InventoryMovementResponseDTO(
                movement.getId(),
                movement.getBookId(),
                title,
                author,
                movement.getTipoMovimiento(),
                movement.getCantidad(),
                movement.getMotivo(),
                movement.getResponsable(),
                disponibles,
                movement.getFechaMovimiento()
        );
    }

    private BookResponseDTO obtenerLibroParaRespuesta(
            Long bookId) {

        try {
            return bookClient.buscarLibroPorId(bookId);

        } catch (Exception ex) {

            log.warn(
                    "No se pudo obtener información del libro ID {} para la respuesta",
                    bookId
            );

            return null;
        }
    }

    private Long obtenerDisponibilidadParaRespuesta(
            Long bookId) {

        try {
            return copiaClient
                    .contarCopiasDisponiblesPorLibro(
                            bookId
                    );

        } catch (Exception ex) {

            log.warn(
                    "No se pudo obtener disponibilidad del libro ID {} para la respuesta",
                    bookId
            );

            return null;
        }
    }
}