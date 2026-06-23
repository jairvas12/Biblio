package com.library.copia_service.service.impl;

import com.library.copia_service.client.BookClient;
import com.library.copia_service.dto.BookResponseDTO;
import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.exception.BusinessException;
import com.library.copia_service.exception.ResourceNotFoundException;
import com.library.copia_service.model.Copia;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.repository.CopiaRepository;
import com.library.copia_service.service.CopiaService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CopiaServiceImpl implements CopiaService {

    private final CopiaRepository copiaRepository;
    private final BookClient bookClient;

    @Override
    public CopiaResponseDTO crearCopia(CopiaRequestDTO requestDTO) {
        log.info("Iniciando creación de copia con código {}", requestDTO.getCodigoCopia());

        validarLibroExiste(requestDTO.getBookId());

        if (copiaRepository.existsByCodigoCopia(requestDTO.getCodigoCopia())) {
            throw new BusinessException("Ya existe una copia con el código: " + requestDTO.getCodigoCopia());
        }

        EstadoCopia estadoInicial = requestDTO.getEstado() != null
                ? requestDTO.getEstado()
                : EstadoCopia.AVAILABLE;

        Copia copia = Copia.builder()
                .codigoCopia(requestDTO.getCodigoCopia())
                .bookId(requestDTO.getBookId())
                .estado(estadoInicial)
                .ubicacion(requestDTO.getUbicacion())
                .observacion(requestDTO.getObservacion())
                .build();

        Copia copiaGuardada = copiaRepository.save(copia);

        log.info("Copia creada correctamente con ID {}", copiaGuardada.getId());

        return convertirAResponseDTO(copiaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopias() {
        log.info("Listando todas las copias");

        return copiaRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CopiaResponseDTO buscarCopiaPorId(Long id) {
        log.info("Buscando copia por ID {}", id);

        Copia copia = obtenerCopiaPorId(id);

        return convertirAResponseDTO(copia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasPorLibro(Long bookId) {
        log.info("Listando copias del libro con ID {}", bookId);

        validarLibroExiste(bookId);

        return copiaRepository.findByBookId(bookId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasDisponiblesPorLibro(Long bookId) {
        log.info("Listando copias disponibles del libro con ID {}", bookId);

        validarLibroExiste(bookId);

        return copiaRepository.findByBookIdAndEstado(bookId, EstadoCopia.AVAILABLE)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopiaResponseDTO> listarCopiasPorEstado(EstadoCopia estado) {
        log.info("Listando copias con estado {}", estado);

        return copiaRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    public CopiaResponseDTO actualizarCopia(Long id, CopiaRequestDTO requestDTO) {
        log.info("Actualizando copia con ID {}", id);

        Copia copia = obtenerCopiaPorId(id);

        validarLibroExiste(requestDTO.getBookId());

        if (copiaRepository.existsByCodigoCopiaAndIdNot(requestDTO.getCodigoCopia(), id)) {
            throw new BusinessException("Ya existe otra copia con el código: " + requestDTO.getCodigoCopia());
        }

        copia.setCodigoCopia(requestDTO.getCodigoCopia());
        copia.setBookId(requestDTO.getBookId());
        copia.setUbicacion(requestDTO.getUbicacion());
        copia.setObservacion(requestDTO.getObservacion());

        if (requestDTO.getEstado() != null) {
            copia.setEstado(requestDTO.getEstado());
        }

        Copia copiaActualizada = copiaRepository.save(copia);

        log.info("Copia actualizada correctamente con ID {}", copiaActualizada.getId());

        return convertirAResponseDTO(copiaActualizada);
    }

    @Override
    public CopiaResponseDTO cambiarEstado(Long id, EstadoCopia estado) {
        log.info("Cambiando estado de copia ID {} a {}", id, estado);

        Copia copia = obtenerCopiaPorId(id);
        copia.setEstado(estado);

        Copia copiaActualizada = copiaRepository.save(copia);

        log.info("Estado actualizado correctamente para copia ID {}", id);

        return convertirAResponseDTO(copiaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarCopiasDisponiblesPorLibro(Long bookId) {
        log.info("Contando copias disponibles para libro ID {}", bookId);

        validarLibroExiste(bookId);

        return copiaRepository.countByBookIdAndEstado(bookId, EstadoCopia.AVAILABLE);
    }

    @Override
    public void eliminarCopia(Long id) {
        log.info("Eliminando copia con ID {}", id);

        Copia copia = obtenerCopiaPorId(id);

        if (copia.getEstado() == EstadoCopia.LOANED) {
            throw new BusinessException("No se puede eliminar una copia que está prestada");
        }

        copiaRepository.delete(copia);

        log.info("Copia eliminada correctamente con ID {}", id);
    }

    private Copia obtenerCopiaPorId(Long id) {
        return copiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe copia con ID: " + id));
    }

    private BookResponseDTO validarLibroExiste(Long bookId) {
        try {
            return bookClient.buscarLibroPorId(bookId);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new ResourceNotFoundException("No existe libro en book-service con ID: " + bookId);
            }

            log.error("Error al comunicarse con book-service. Status: {}", ex.status());
            throw new BusinessException("No se pudo validar el libro en book-service");
        }
    }

    private BookResponseDTO obtenerLibroParaRespuesta(Long bookId) {
        try {
            return bookClient.buscarLibroPorId(bookId);
        } catch (Exception ex) {
            log.warn("No se pudo obtener información del libro ID {} para la respuesta", bookId);
            return null;
        }
    }

    private CopiaResponseDTO convertirAResponseDTO(Copia copia) {
        BookResponseDTO libro = obtenerLibroParaRespuesta(copia.getBookId());

        return CopiaResponseDTO.builder()
                .id(copia.getId())
                .codigoCopia(copia.getCodigoCopia())
                .bookId(copia.getBookId())
                .bookTitle(libro != null ? libro.getTitle() : null)
                .bookAuthor(libro != null ? libro.getAuthor() : null)
                .estado(copia.getEstado())
                .ubicacion(copia.getUbicacion())
                .observacion(copia.getObservacion())
                .fechaRegistro(copia.getFechaRegistro())
                .build();
    }
}