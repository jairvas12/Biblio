package com.library.copia_service.service;

import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.model.EstadoCopia;

import java.util.List;

public interface CopiaService {

    CopiaResponseDTO crearCopia(CopiaRequestDTO requestDTO);

    List<CopiaResponseDTO> listarCopias();

    CopiaResponseDTO buscarCopiaPorId(Long id);

    List<CopiaResponseDTO> listarCopiasPorLibro(Long bookId);

    List<CopiaResponseDTO> listarCopiasDisponiblesPorLibro(Long bookId);

    List<CopiaResponseDTO> listarCopiasPorEstado(EstadoCopia estado);

    CopiaResponseDTO actualizarCopia(Long id, CopiaRequestDTO requestDTO);

    CopiaResponseDTO cambiarEstado(Long id, EstadoCopia estado);

    Long contarCopiasDisponiblesPorLibro(Long bookId);

    void eliminarCopia(Long id);
}