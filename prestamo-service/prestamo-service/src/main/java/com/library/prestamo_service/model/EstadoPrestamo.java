package com.library.prestamo_service.model;

/**
 * Representa los estados posibles dentro del ciclo de vida de un préstamo.
 */
public enum EstadoPrestamo {

    /**
     * El préstamo se encuentra vigente y todavía no ha sido devuelto.
     */
    ACTIVO,

    /**
     * La fecha de devolución esperada ya pasó
     * y la copia todavía no ha sido devuelta.
     */
    ATRASADO,

    /**
     * La copia fue entregada correctamente.
     */
    DEVUELTO,

    /**
     * El préstamo fue cancelado antes de completar su flujo normal.
     */
    CANCELADO
}