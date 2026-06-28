package com.library.category_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler =
                new GlobalExceptionHandler();

        request =
                new MockHttpServletRequest();
    }

    @Test
    void handleResourceNotFoundException_shouldReturnNotFound() {
        request.setRequestURI(
                "/categories/99"
        );

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Categoría no encontrada con id: 99"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler
                        .handleResourceNotFoundException(
                                exception,
                                request
                        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(
                response.getBody().getTimestamp()
        );

        assertEquals(
                404,
                response.getBody().getStatus()
        );

        assertEquals(
                "NOT FOUND",
                response.getBody().getError()
        );

        assertEquals(
                "Categoría no encontrada con id: 99",
                response.getBody().getMessage()
        );

        assertEquals(
                "/categories/99",
                response.getBody().getPath()
        );

        assertNull(
                response.getBody()
                        .getValidationErrors()
        );
    }

    @Test
    void handleBusinessException_shouldReturnBadRequest() {
        request.setRequestURI(
                "/categories"
        );

        BusinessException exception =
                new BusinessException(
                        "Ya existe una categoría con el nombre: Historia"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler
                        .handleBusinessException(
                                exception,
                                request
                        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "BUSINESS ERROR",
                response.getBody().getError()
        );

        assertEquals(
                "Ya existe una categoría con el nombre: Historia",
                response.getBody().getMessage()
        );

        assertEquals(
                "/categories",
                response.getBody().getPath()
        );
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        request.setRequestURI(
                "/categories"
        );

        Exception exception =
                new Exception(
                        "Error inesperado"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler
                        .handleGeneralException(
                                exception,
                                request
                        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                500,
                response.getBody().getStatus()
        );

        assertEquals(
                "INTERNAL SERVER ERROR",
                response.getBody().getError()
        );

        assertEquals(
                "Ocurrió un error interno en el servidor",
                response.getBody().getMessage()
        );

        assertEquals(
                "/categories",
                response.getBody().getPath()
        );
    }
}
