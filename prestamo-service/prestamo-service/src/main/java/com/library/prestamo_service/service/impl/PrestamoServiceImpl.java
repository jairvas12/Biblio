package com.library.prestamo_service.service.impl;

import com.library.prestamo_service.client.CopiaClient;
import com.library.prestamo_service.client.dto.CopiaRemotaDTO;
import com.library.prestamo_service.client.dto.EstadoCopiaRemota;
import com.library.prestamo_service.dto.CrearPrestamoRequestDTO;
import com.library.prestamo_service.dto.DevolverPrestamoRequestDTO;
import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.exception.RecursoNoEncontradoException;
import com.library.prestamo_service.exception.ReglaNegocioException;
import com.library.prestamo_service.exception.ServicioRemotoException;
import com.library.prestamo_service.mapper.PrestamoMapper;
import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.model.Prestamo;
import com.library.prestamo_service.repository.PrestamoRepository;
import com.library.prestamo_service.service.PrestamoService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrestamoServiceImpl implements PrestamoService {

    private static final int MAX_PRESTAMOS_VIGENTES = 3;

    private static final List<EstadoPrestamo> ESTADOS_VIGENTES =
            List.of(
                    EstadoPrestamo.ACTIVO,
                    EstadoPrestamo.ATRASADO
            );

    private final PrestamoRepository prestamoRepository;
    private final PrestamoMapper prestamoMapper;
    private final CopiaClient copiaClient;

    @Override
    public PrestamoResponseDTO crearPrestamo(
            CrearPrestamoRequestDTO requestDTO
    ) {
        log.info(
                "Iniciando préstamo para usuario ID {} y copia ID {}",
                requestDTO.getUsuarioId(),
                requestDTO.getCopiaId()
        );

        CopiaRemotaDTO copia =
                obtenerCopiaRemota(
                        requestDTO.getCopiaId()
                );

        validarCopiaDisponible(copia);

        validarCopiaSinPrestamoVigente(
                requestDTO.getCopiaId()
        );

        validarLimitePrestamosUsuario(
                requestDTO.getUsuarioId()
        );

        LocalDate fechaPrestamo = LocalDate.now();

        Prestamo prestamo = Prestamo.builder()
                .usuarioId(requestDTO.getUsuarioId())
                .copiaId(requestDTO.getCopiaId())
                .fechaPrestamo(fechaPrestamo)
                .fechaVencimiento(
                        fechaPrestamo.plusDays(
                                requestDTO.getDiasPrestamo()
                        )
                )
                .fechaDevolucion(null)
                .estado(EstadoPrestamo.ACTIVO)
                .observacion(
                        normalizarObservacion(
                                requestDTO.getObservacion()
                        )
                )
                .build();

        boolean copiaMarcadaComoPrestada = false;

        try {
            cambiarEstadoCopia(
                    requestDTO.getCopiaId(),
                    EstadoCopiaRemota.LOANED
            );

            copiaMarcadaComoPrestada = true;

            Prestamo prestamoGuardado =
                    prestamoRepository.saveAndFlush(
                            prestamo
                    );

            log.info(
                    "Préstamo creado correctamente con ID {}",
                    prestamoGuardado.getId()
            );

            return prestamoMapper.convertirAResponseDTO(
                    prestamoGuardado
            );

        } catch (RuntimeException exception) {

            if (copiaMarcadaComoPrestada) {
                intentarRestaurarCopiaDisponible(
                        requestDTO.getCopiaId()
                );
            }

            log.error(
                    "No fue posible completar el préstamo "
                            + "para usuario ID {} y copia ID {}",
                    requestDTO.getUsuarioId(),
                    requestDTO.getCopiaId(),
                    exception
            );

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoResponseDTO obtenerPorId(
            Long prestamoId
    ) {
        log.info(
                "Buscando préstamo con ID {}",
                prestamoId
        );

        Prestamo prestamo =
                obtenerEntidadPorId(
                        prestamoId
                );

        return prestamoMapper.convertirAResponseDTO(
                prestamo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarTodos() {
        log.info("Listando todos los préstamos");

        return prestamoRepository.findAll()
                .stream()
                .map(prestamoMapper::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPorUsuario(
            Long usuarioId
    ) {
        log.info(
                "Listando préstamos del usuario ID {}",
                usuarioId
        );

        return prestamoRepository
                .findByUsuarioIdOrderByFechaPrestamoDesc(
                        usuarioId
                )
                .stream()
                .map(prestamoMapper::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPorCopia(
            Long copiaId
    ) {
        log.info(
                "Listando préstamos de la copia ID {}",
                copiaId
        );

        return prestamoRepository
                .findByCopiaIdOrderByFechaPrestamoDesc(
                        copiaId
                )
                .stream()
                .map(prestamoMapper::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPorEstado(
            EstadoPrestamo estado
    ) {
        log.info(
                "Listando préstamos con estado {}",
                estado
        );

        return prestamoRepository
                .findByEstadoOrderByFechaVencimientoAsc(
                        estado
                )
                .stream()
                .map(prestamoMapper::convertirAResponseDTO)
                .toList();
    }

    @Override
    public PrestamoResponseDTO devolverPrestamo(
            Long prestamoId,
            DevolverPrestamoRequestDTO requestDTO
    ) {
        log.info(
                "Iniciando devolución del préstamo ID {}",
                prestamoId
        );

        Prestamo prestamo =
                obtenerEntidadPorId(
                        prestamoId
                );

        validarPrestamoPuedeDevolverse(
                prestamo
        );

        CopiaRemotaDTO copia =
                obtenerCopiaRemota(
                        prestamo.getCopiaId()
                );

        validarCopiaPrestadaParaDevolucion(
                copia
        );

        EstadoPrestamo estadoAnterior =
                prestamo.getEstado();

        LocalDate fechaDevolucionAnterior =
                prestamo.getFechaDevolucion();

        String observacionAnterior =
                prestamo.getObservacion();

        boolean copiaMarcadaDisponible = false;

        try {
            cambiarEstadoCopia(
                    prestamo.getCopiaId(),
                    EstadoCopiaRemota.AVAILABLE
            );

            copiaMarcadaDisponible = true;

            prestamo.setFechaDevolucion(
                    LocalDate.now()
            );

            prestamo.setEstado(
                    EstadoPrestamo.DEVUELTO
            );

            if (requestDTO != null) {
                String observacionDevolucion =
                        normalizarObservacion(
                                requestDTO.getObservacion()
                        );

                if (observacionDevolucion != null) {
                    prestamo.setObservacion(
                            observacionDevolucion
                    );
                }
            }

            Prestamo prestamoActualizado =
                    prestamoRepository.saveAndFlush(
                            prestamo
                    );

            log.info(
                    "Préstamo ID {} devuelto correctamente. "
                            + "Copia ID {} disponible nuevamente",
                    prestamoId,
                    prestamo.getCopiaId()
            );

            return prestamoMapper.convertirAResponseDTO(
                    prestamoActualizado
            );

        } catch (RuntimeException exception) {

            if (copiaMarcadaDisponible) {
                intentarRestaurarCopiaPrestada(
                        prestamo.getCopiaId()
                );
            }

            prestamo.setEstado(
                    estadoAnterior
            );

            prestamo.setFechaDevolucion(
                    fechaDevolucionAnterior
            );

            prestamo.setObservacion(
                    observacionAnterior
            );

            log.error(
                    "No fue posible completar la devolución "
                            + "del préstamo ID {}",
                    prestamoId,
                    exception
            );

            throw exception;
        }
    }

    @Override
    public int actualizarPrestamosAtrasados() {
        LocalDate fechaActual =
                LocalDate.now();

        log.info(
                "Buscando préstamos activos vencidos antes de {}",
                fechaActual
        );

        List<Prestamo> prestamosVencidos =
                prestamoRepository
                        .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                                EstadoPrestamo.ACTIVO,
                                fechaActual
                        );

        for (Prestamo prestamo : prestamosVencidos) {
            prestamo.setEstado(
                    EstadoPrestamo.ATRASADO
            );

            log.info(
                    "Préstamo ID {} marcado como ATRASADO",
                    prestamo.getId()
            );
        }

        if (!prestamosVencidos.isEmpty()) {
            prestamoRepository.saveAll(
                    prestamosVencidos
            );
        }

        log.info(
                "Cantidad de préstamos actualizados a ATRASADO: {}",
                prestamosVencidos.size()
        );

        return prestamosVencidos.size();
    }

    private Prestamo obtenerEntidadPorId(
            Long prestamoId
    ) {
        return prestamoRepository.findById(
                        prestamoId
                )
                .orElseThrow(
                        () -> new RecursoNoEncontradoException(
                                "No se encontró el préstamo con ID "
                                        + prestamoId
                        )
                );
    }

    private CopiaRemotaDTO obtenerCopiaRemota(
            Long copiaId
    ) {
        try {
            CopiaRemotaDTO copia =
                    copiaClient.obtenerCopiaPorId(
                            copiaId
                    );

            if (copia == null
                    || copia.getId() == null
                    || copia.getEstado() == null) {

                throw new ServicioRemotoException(
                        "copia-service devolvió una respuesta incompleta"
                );
            }

            return copia;

        } catch (RetryableException exception) {

            log.error(
                    "No fue posible conectar con copia-service "
                            + "al consultar la copia ID {}",
                    copiaId,
                    exception
            );

            throw new ServicioRemotoException(
                    "No fue posible conectar con copia-service",
                    exception
            );

        } catch (FeignException exception) {

            log.error(
                    "Error Feign no controlado al consultar "
                            + "la copia ID {}. Estado HTTP: {}",
                    copiaId,
                    exception.status(),
                    exception
            );

            throw new ServicioRemotoException(
                    "Ocurrió un error al consultar copia-service",
                    exception
            );
        }
    }

    private void validarCopiaDisponible(
            CopiaRemotaDTO copia
    ) {
        if (copia.getEstado()
                != EstadoCopiaRemota.AVAILABLE) {

            log.warn(
                    "La copia ID {} no está disponible. Estado actual: {}",
                    copia.getId(),
                    copia.getEstado()
            );

            throw new ReglaNegocioException(
                    "La copia con ID "
                            + copia.getId()
                            + " no está disponible para préstamo. "
                            + "Estado actual: "
                            + copia.getEstado()
            );
        }
    }

    private void validarCopiaSinPrestamoVigente(
            Long copiaId
    ) {
        boolean poseePrestamoVigente =
                prestamoRepository
                        .existsByCopiaIdAndEstadoIn(
                                copiaId,
                                ESTADOS_VIGENTES
                        );

        if (poseePrestamoVigente) {
            log.warn(
                    "La copia ID {} ya posee un préstamo vigente",
                    copiaId
            );

            throw new ReglaNegocioException(
                    "La copia con ID "
                            + copiaId
                            + " ya posee un préstamo activo o atrasado"
            );
        }
    }

    private void validarLimitePrestamosUsuario(
            Long usuarioId
    ) {
        long cantidadPrestamosVigentes =
                prestamoRepository
                        .countByUsuarioIdAndEstadoIn(
                                usuarioId,
                                ESTADOS_VIGENTES
                        );

        if (cantidadPrestamosVigentes
                >= MAX_PRESTAMOS_VIGENTES) {

            log.warn(
                    "El usuario ID {} alcanzó el máximo "
                            + "de {} préstamos vigentes",
                    usuarioId,
                    MAX_PRESTAMOS_VIGENTES
            );

            throw new ReglaNegocioException(
                    "El usuario con ID "
                            + usuarioId
                            + " alcanzó el máximo de "
                            + MAX_PRESTAMOS_VIGENTES
                            + " préstamos vigentes"
            );
        }
    }

    private void validarPrestamoPuedeDevolverse(
            Prestamo prestamo
    ) {
        if (prestamo.getEstado()
                == EstadoPrestamo.DEVUELTO) {

            throw new ReglaNegocioException(
                    "El préstamo con ID "
                            + prestamo.getId()
                            + " ya fue devuelto"
            );
        }

        if (prestamo.getEstado()
                == EstadoPrestamo.CANCELADO) {

            throw new ReglaNegocioException(
                    "No se puede devolver un préstamo cancelado"
            );
        }

        if (prestamo.getEstado()
                != EstadoPrestamo.ACTIVO
                && prestamo.getEstado()
                != EstadoPrestamo.ATRASADO) {

            throw new ReglaNegocioException(
                    "El préstamo no se encuentra en un estado válido "
                            + "para realizar la devolución"
            );
        }
    }

    private void validarCopiaPrestadaParaDevolucion(
            CopiaRemotaDTO copia
    ) {
        if (copia.getEstado()
                != EstadoCopiaRemota.LOANED) {

            log.warn(
                    "La copia ID {} asociada al préstamo "
                            + "no está en estado LOANED. Estado actual: {}",
                    copia.getId(),
                    copia.getEstado()
            );

            throw new ReglaNegocioException(
                    "La copia con ID "
                            + copia.getId()
                            + " no se encuentra en estado LOANED. "
                            + "Estado actual: "
                            + copia.getEstado()
            );
        }
    }

    private CopiaRemotaDTO cambiarEstadoCopia(
            Long copiaId,
            EstadoCopiaRemota nuevoEstado
    ) {
        try {
            CopiaRemotaDTO copiaActualizada =
                    copiaClient.cambiarEstado(
                            copiaId,
                            nuevoEstado
                    );

            if (copiaActualizada == null
                    || copiaActualizada.getId() == null
                    || copiaActualizada.getEstado() == null) {

                throw new ServicioRemotoException(
                        "copia-service devolvió una respuesta incompleta "
                                + "al cambiar el estado de la copia"
                );
            }

            if (!copiaId.equals(
                    copiaActualizada.getId()
            )) {
                throw new ServicioRemotoException(
                        "copia-service respondió con una copia diferente"
                );
            }

            if (copiaActualizada.getEstado()
                    != nuevoEstado) {

                throw new ServicioRemotoException(
                        "copia-service no confirmó correctamente "
                                + "el nuevo estado de la copia"
                );
            }

            return copiaActualizada;

        } catch (RetryableException exception) {

            log.error(
                    "No fue posible conectar con copia-service "
                            + "al cambiar la copia ID {} a {}",
                    copiaId,
                    nuevoEstado,
                    exception
            );

            throw new ServicioRemotoException(
                    "No fue posible conectar con copia-service",
                    exception
            );

        } catch (FeignException exception) {

            log.error(
                    "Error Feign no controlado al cambiar "
                            + "la copia ID {} a {}. Estado HTTP: {}",
                    copiaId,
                    nuevoEstado,
                    exception.status(),
                    exception
            );

            throw new ServicioRemotoException(
                    "Ocurrió un error al actualizar la copia "
                            + "en copia-service",
                    exception
            );
        }
    }

    private void intentarRestaurarCopiaDisponible(
            Long copiaId
    ) {
        try {
            log.warn(
                    "Intentando restaurar la copia ID {} a AVAILABLE",
                    copiaId
            );

            cambiarEstadoCopia(
                    copiaId,
                    EstadoCopiaRemota.AVAILABLE
            );

            log.info(
                    "Copia ID {} restaurada correctamente a AVAILABLE",
                    copiaId
            );

        } catch (RuntimeException exception) {

            log.error(
                    "No fue posible restaurar la copia ID {} "
                            + "después del fallo del préstamo",
                    copiaId,
                    exception
            );
        }
    }

    private void intentarRestaurarCopiaPrestada(
            Long copiaId
    ) {
        try {
            log.warn(
                    "Intentando restaurar la copia ID {} a LOANED",
                    copiaId
            );

            cambiarEstadoCopia(
                    copiaId,
                    EstadoCopiaRemota.LOANED
            );

            log.info(
                    "Copia ID {} restaurada correctamente a LOANED",
                    copiaId
            );

        } catch (RuntimeException exception) {

            log.error(
                    "No fue posible restaurar la copia ID {} "
                            + "a LOANED después del fallo de devolución",
                    copiaId,
                    exception
            );
        }
    }

    private String normalizarObservacion(
            String observacion
    ) {
        if (observacion == null
                || observacion.isBlank()) {

            return null;
        }

        return observacion.trim();
    }
}