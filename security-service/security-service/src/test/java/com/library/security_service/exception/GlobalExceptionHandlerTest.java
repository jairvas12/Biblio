package com.library.security_service.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = new MockHttpServletRequest();
        request.setRequestURI("/security/events/1");
    }

    @Test
    void handleNotFoundDebeRetornar404() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Evento de seguridad no encontrado"
                );

        ResponseEntity<ErrorResponse> result =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(404, result.getBody().status());

        assertEquals(
                "Evento de seguridad no encontrado",
                result.getBody().message()
        );

        assertEquals(
                "/security/events/1",
                result.getBody().path()
        );
    }

    @Test
    void handleBusinessDebeRetornar400() {

        BusinessException exception =
                new BusinessException(
                        "El ID debe ser mayor que cero"
                );

        ResponseEntity<ErrorResponse> result =
                handler.handleBusiness(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(400, result.getBody().status());

        assertEquals(
                "El ID debe ser mayor que cero",
                result.getBody().message()
        );
    }

    @Test
    void handleValidationDebeRetornarErroresDeCampos() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                new FieldError(
                        "securityEventRequestDTO",
                        "description",
                        "La descripción es obligatoria"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> result =
                handler.handleValidation(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertNotNull(
                result.getBody().validationErrors()
        );

        assertEquals(
                "La descripción es obligatoria",
                result.getBody()
                        .validationErrors()
                        .get("description")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolationDebeRetornar400() {

        ConstraintViolation<Object> violation =
                mock(ConstraintViolation.class);

        Path propertyPath =
                mock(Path.class);

        when(violation.getPropertyPath())
                .thenReturn(propertyPath);

        when(propertyPath.toString())
                .thenReturn("userId");

        when(violation.getMessage())
                .thenReturn(
                        "El ID del usuario debe ser mayor que cero"
                );

        ConstraintViolationException exception =
                new ConstraintViolationException(
                        Set.of(violation)
                );

        ResponseEntity<ErrorResponse> result =
                handler.handleConstraintViolation(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertNotNull(
                result.getBody().validationErrors()
        );

        assertEquals(
                "El ID del usuario debe ser mayor que cero",
                result.getBody()
                        .validationErrors()
                        .get("userId")
        );
    }

    @Test
    void handleGenericDebeRetornar500() {

        Exception exception =
                new RuntimeException(
                        "Error inesperado"
                );

        ResponseEntity<ErrorResponse> result =
                handler.handleGeneric(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(500, result.getBody().status());

        assertEquals(
                "Ocurrió un error interno en el servidor",
                result.getBody().message()
        );
    }
}