package com.library.prestamo_service.exception;

public class ServicioRemotoException extends RuntimeException {

    public ServicioRemotoException(String mensaje) {
        super(mensaje);
    }

    public ServicioRemotoException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}