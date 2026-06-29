package com.library.inventory_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();

        webRequest = mock(WebRequest.class);

        when(webRequest.getDescription(false))
                .thenReturn("uri=/inventory/movements/99");
    }

    @Test
    void handleNotFoundDevuelve404() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Movimiento no encontrado"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleNotFound(
                        exception,
                        webRequest
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }

    @Test
    void handleBusinessDevuelve400() {
        BusinessException exception =
                new BusinessException(
                        "Movimiento inválido"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleBusiness(
                        exception,
                        webRequest
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }

    @Test
    void handleRemoteServiceDevuelve503() {
        RemoteServiceException exception =
                new RemoteServiceException(
                        "Servicio externo no disponible"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleRemoteService(
                        exception,
                        webRequest
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }

    @Test
    void handleGeneralDevuelve500() {
        Exception exception =
                new RuntimeException(
                        "Error inesperado"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGeneral(
                        exception,
                        webRequest
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }
}