package com.library.copia_service.service.impl;

import com.library.copia_service.client.BookClient;
import com.library.copia_service.dto.BookResponseDTO;
import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.exception.BusinessException;
import com.library.copia_service.exception.RemoteServiceException;
import com.library.copia_service.exception.ResourceNotFoundException;
import com.library.copia_service.mapper.CopiaMapper;
import com.library.copia_service.model.Copia;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.repository.CopiaRepository;
import com.library.copia_service.service.CopiaService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CopiaServiceImpl implements CopiaService {

    private final CopiaRepository copiaRepository;
    private final BookClient bookClient;
    private final CopiaMapper copiaMapper;

    @Override
    public CopiaResponseDTO crearCopia(CopiaRequestDTO requestDTO) {
        validarSolicitud(requestDTO);

        log.info(
                "Iniciando creación de copia con código {}",
                requestDTO.getCodigoCopia()
        );

        validarEstadoInicial(requestDTO.getEstado());

        BookResponseDTO libro = validarLibroExiste(requestDTO.getBookId());

        if (copiaRepository.existsByCodigoCopia(requestDTO.getCodigoCopia())) {
            throw new BusinessException(
                    "Ya existe una copia con el código: "
                            + requestDTO.getCodigoCopia()
            );
        }

        Copia copia = copiaMapper.toEntity(requestDTO);
        Copia copiaGuardada = copiaRepository.save(copia);

        log.info(
                "Copia creada correctamente con ID {} y estado {}",
                copiaGuardada.getId(),
                copiaGuardada.getEstado()
        );

        return copiaMapper.toResponseDTO(copiaGuardada, libro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopias() {
        log.info("Listando todas las copias");

        return convertirListaAResponseDTO(copiaRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CopiaResponseDTO buscarCopiaPorId(Long id) {
        validarIdentificador(id, "copia");

        log.info("Buscando copia por ID {}", id);

        Copia copia = obtenerCopiaPorId(id);
        BookResponseDTO libro = obtenerLibroParaRespuesta(copia.getBookId());

        return copiaMapper.toResponseDTO(copia, libro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasPorLibro(Long bookId) {
        validarIdentificador(bookId, "libro");

        log.info("Listando copias del libro con ID {}", bookId);

        BookResponseDTO libro = validarLibroExiste(bookId);

        return copiaRepository.findByBookId(bookId)
                .stream()
                .map(copia -> copiaMapper.toResponseDTO(copia, libro))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasDisponiblesPorLibro(Long bookId) {
        validarIdentificador(bookId, "libro");

        log.info(
                "Listando copias disponibles del libro con ID {}",
                bookId
        );

        BookResponseDTO libro = validarLibroExiste(bookId);

        return copiaRepository
                .findByBookIdAndEstado(bookId, EstadoCopia.AVAILABLE)
                .stream()
                .map(copia -> copiaMapper.toResponseDTO(copia, libro))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasPorEstado(EstadoCopia estado) {
        if (estado == null) {
            throw new BusinessException(
                    "El estado de la copia es obligatorio"
            );
        }

        log.info("Listando copias con estado {}", estado);

        return convertirListaAResponseDTO(
                copiaRepository.findByEstado(estado)
        );
    }

    @Override
    public CopiaResponseDTO actualizarCopia(
            Long id,
            CopiaRequestDTO requestDTO
    ) {
        validarIdentificador(id, "copia");
        validarSolicitud(requestDTO);

        log.info("Actualizando copia con ID {}", id);

        Copia copia = obtenerCopiaPorId(id);

        validarEstadoEnActualizacion(
                copia.getEstado(),
                requestDTO.getEstado()
        );

        BookResponseDTO libro = validarLibroExiste(requestDTO.getBookId());

        if (copiaRepository.existsByCodigoCopiaAndIdNot(
                requestDTO.getCodigoCopia(),
                id
        )) {
            throw new BusinessException(
                    "Ya existe otra copia con el código: "
                            + requestDTO.getCodigoCopia()
            );
        }

        copiaMapper.updateEntity(copia, requestDTO);

        Copia copiaActualizada = copiaRepository.save(copia);

        log.info(
                "Copia actualizada correctamente con ID {}",
                copiaActualizada.getId()
        );

        return copiaMapper.toResponseDTO(copiaActualizada, libro);
    }

    @Override
    public CopiaResponseDTO cambiarEstado(
            Long id,
            EstadoCopia nuevoEstado
    ) {
        validarIdentificador(id, "copia");

        if (nuevoEstado == null) {
            throw new BusinessException(
                    "El nuevo estado de la copia es obligatorio"
            );
        }

        log.info(
                "Cambiando estado de copia ID {} a {}",
                id,
                nuevoEstado
        );

        Copia copia = obtenerCopiaPorId(id);
        EstadoCopia estadoActual = copia.getEstado();

        if (estadoActual == nuevoEstado) {
            log.info(
                    "La copia ID {} ya se encuentra en estado {}",
                    id,
                    nuevoEstado
            );

            return copiaMapper.toResponseDTO(
                    copia,
                    obtenerLibroParaRespuesta(copia.getBookId())
            );
        }

        validarTransicionEstado(estadoActual, nuevoEstado);

        copia.setEstado(nuevoEstado);

        Copia copiaActualizada = copiaRepository.save(copia);

        log.info(
                "Estado de copia ID {} actualizado de {} a {}",
                id,
                estadoActual,
                nuevoEstado
        );

        return copiaMapper.toResponseDTO(
                copiaActualizada,
                obtenerLibroParaRespuesta(copiaActualizada.getBookId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarCopiasDisponiblesPorLibro(Long bookId) {
        validarIdentificador(bookId, "libro");

        log.info(
                "Contando copias disponibles para libro ID {}",
                bookId
        );

        validarLibroExiste(bookId);

        return copiaRepository.countByBookIdAndEstado(
                bookId,
                EstadoCopia.AVAILABLE
        );
    }

    @Override
    public void eliminarCopia(Long id) {
        validarIdentificador(id, "copia");

        log.info("Eliminando copia con ID {}", id);

        Copia copia = obtenerCopiaPorId(id);

        if (copia.getEstado() == EstadoCopia.LOANED) {
            throw new BusinessException(
                    "No se puede eliminar una copia que está prestada"
            );
        }

        if (copia.getEstado() == EstadoCopia.RESERVED) {
            throw new BusinessException(
                    "No se puede eliminar una copia que está reservada"
            );
        }

        copiaRepository.delete(copia);

        log.info("Copia eliminada correctamente con ID {}", id);
    }

    private Copia obtenerCopiaPorId(Long id) {
        return copiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una copia con ID: " + id
                        )
                );
    }

    private BookResponseDTO validarLibroExiste(Long bookId) {
        validarIdentificador(bookId, "libro");

        try {
            BookResponseDTO libro = bookClient.buscarLibroPorId(bookId);

            if (libro == null) {
                throw new RemoteServiceException(
                        "book-service devolvió una respuesta vacía "
                                + "para el libro ID " + bookId
                );
            }

            return libro;
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new ResourceNotFoundException(
                        "No existe un libro en book-service con ID: "
                                + bookId
                );
            }

            log.error(
                    "Error HTTP al comunicarse con book-service. "
                            + "Libro ID: {}, status: {}",
                    bookId,
                    ex.status(),
                    ex
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con book-service",
                    ex
            );
        } catch (RemoteServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Error inesperado al comunicarse con book-service "
                            + "para el libro ID {}",
                    bookId,
                    ex
            );

            throw new RemoteServiceException(
                    "No fue posible comunicarse con book-service",
                    ex
            );
        }
    }

    private BookResponseDTO obtenerLibroParaRespuesta(Long bookId) {
        try {
            return bookClient.buscarLibroPorId(bookId);
        } catch (Exception ex) {
            log.warn(
                    "No fue posible obtener información del libro ID {} "
                            + "para enriquecer la respuesta",
                    bookId
            );

            return null;
        }
    }

    private List<CopiaResponseDTO> convertirListaAResponseDTO(
            List<Copia> copias
    ) {
        Map<Long, BookResponseDTO> librosPorId = new HashMap<>();

        return copias.stream()
                .map(copia -> {
                    Long bookId = copia.getBookId();

                    if (!librosPorId.containsKey(bookId)) {
                        librosPorId.put(
                                bookId,
                                obtenerLibroParaRespuesta(bookId)
                        );
                    }

                    return copiaMapper.toResponseDTO(
                            copia,
                            librosPorId.get(bookId)
                    );
                })
                .toList();
    }

    private void validarSolicitud(CopiaRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BusinessException(
                    "La solicitud de copia no puede ser nula"
            );
        }
    }

    private void validarIdentificador(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    "El identificador de " + nombre
                            + " debe ser mayor que cero"
            );
        }
    }

    private void validarEstadoInicial(EstadoCopia estadoSolicitado) {
        if (estadoSolicitado != null
                && estadoSolicitado != EstadoCopia.AVAILABLE) {
            throw new BusinessException(
                    "Toda copia nueva debe comenzar en estado AVAILABLE"
            );
        }
    }

    private void validarEstadoEnActualizacion(
            EstadoCopia estadoActual,
            EstadoCopia estadoSolicitado
    ) {
        if (estadoSolicitado != null
                && estadoSolicitado != estadoActual) {
            throw new BusinessException(
                    "El estado de una copia debe modificarse mediante "
                            + "el endpoint PATCH /copias/{id}/estado"
            );
        }
    }

    private void validarTransicionEstado(
            EstadoCopia estadoActual,
            EstadoCopia nuevoEstado
    ) {
        if (estadoActual == null) {
            throw new BusinessException(
                    "La copia no posee un estado actual válido"
            );
        }

        boolean transicionPermitida = switch (estadoActual) {
            case AVAILABLE ->
                    nuevoEstado == EstadoCopia.RESERVED
                            || nuevoEstado == EstadoCopia.LOANED
                            || nuevoEstado == EstadoCopia.DAMAGED
                            || nuevoEstado == EstadoCopia.LOST;

            case RESERVED ->
                    nuevoEstado == EstadoCopia.AVAILABLE
                            || nuevoEstado == EstadoCopia.LOANED
                            || nuevoEstado == EstadoCopia.DAMAGED
                            || nuevoEstado == EstadoCopia.LOST;

            case LOANED ->
                    nuevoEstado == EstadoCopia.AVAILABLE
                            || nuevoEstado == EstadoCopia.DAMAGED
                            || nuevoEstado == EstadoCopia.LOST;

            case DAMAGED ->
                    nuevoEstado == EstadoCopia.AVAILABLE
                            || nuevoEstado == EstadoCopia.LOST;

            case LOST ->
                    nuevoEstado == EstadoCopia.AVAILABLE;
        };

        if (!transicionPermitida) {
            throw new BusinessException(
                    "No se permite cambiar una copia de "
                            + estadoActual + " a " + nuevoEstado
            );
        }
    }
}