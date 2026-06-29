package com.library.prestamo_service.service;

import com.library.prestamo_service.dto.CrearPrestamoRequestDTO;
import com.library.prestamo_service.dto.DevolverPrestamoRequestDTO;
import com.library.prestamo_service.dto.PrestamoResponseDTO;
import com.library.prestamo_service.model.EstadoPrestamo;

import java.util.List;

public interface PrestamoService {

    /**
     * Crea un préstamo nuevo después de validar
     * al usuario, la copia y las reglas del dominio.
     */
    PrestamoResponseDTO crearPrestamo(
            CrearPrestamoRequestDTO requestDTO
    );

    /**
     * Busca un préstamo por su identificador.
     */
    PrestamoResponseDTO obtenerPorId(
            Long prestamoId
    );

    /**
     * Devuelve todos los préstamos registrados.
     */
    List<PrestamoResponseDTO> listarTodos();

    /**
     * Devuelve el historial de préstamos de un usuario.
     */
    List<PrestamoResponseDTO> listarPorUsuario(
            Long usuarioId
    );

    /**
     * Devuelve el historial de préstamos asociado
     * a una copia determinada.
     */
    List<PrestamoResponseDTO> listarPorCopia(
            Long copiaId
    );

    /**
     * Busca préstamos según su estado.
     */
    List<PrestamoResponseDTO> listarPorEstado(
            EstadoPrestamo estado
    );

    /**
     * Registra la devolución de una copia prestada.
     */
    PrestamoResponseDTO devolverPrestamo(
            Long prestamoId,
            DevolverPrestamoRequestDTO requestDTO
    );

    /**
     * Busca préstamos activos vencidos y cambia
     * su estado a ATRASADO.
     *
     * Retorna la cantidad de préstamos actualizados.
     */
    int actualizarPrestamosAtrasados();
}