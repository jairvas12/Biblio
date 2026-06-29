package com.library.prestamo_service.client.dto;

/**
 * Representa los estados entregados por copia-service.
 *
 * Los nombres deben coincidir exactamente con el enum
 * EstadoCopia existente en copia-service.
 */
public enum EstadoCopiaRemota {

    AVAILABLE,
    LOANED,
    DAMAGED,
    LOST,
    RESERVED
}