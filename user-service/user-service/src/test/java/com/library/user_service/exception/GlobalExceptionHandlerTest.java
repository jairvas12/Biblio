package com.library.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {

        exceptionHandler =
                new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFound_shouldReturnNotFound() {

        UserNotFoundException exception =
                new UserNotFoundException(
                        "Usuario no encontrado"
                );

        HttpServletRequest request =
                createRequest(
                        "GET",
                        "/users/9999"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleUserNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertNotNull(
                body.getTimestamp()
        );

        assertEquals(
                404,
                body.getStatus()
        );

        assertEquals(
                "USER NOT FOUND",
                body.getError()
        );

        assertEquals(
                "Usuario no encontrado",
                body.getMessage()
        );

        assertEquals(
                "/users/9999",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleEmailAlreadyExists_shouldReturnConflict() {

        EmailAlreadyExistsException exception =
                new EmailAlreadyExistsException(
                        "El correo ya existe"
                );

        HttpServletRequest request =
                createRequest(
                        "POST",
                        "/users"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleEmailAlreadyExists(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                409,
                body.getStatus()
        );

        assertEquals(
                "EMAIL ALREADY EXISTS",
                body.getError()
        );

        assertEquals(
                "El correo ya existe",
                body.getMessage()
        );

        assertEquals(
                "/users",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleInvalidRole_shouldReturnBadRequest() {

        InvalidRoleException exception =
                new InvalidRoleException(
                        "El rol no es válido"
                );

        HttpServletRequest request =
                createRequest(
                        "PUT",
                        "/users/3"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidRole(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                400,
                body.getStatus()
        );

        assertEquals(
                "INVALID ROLE",
                body.getError()
        );

        assertEquals(
                "El rol no es válido",
                body.getMessage()
        );

        assertEquals(
                "/users/3",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleEmailChangeNotAllowed_shouldReturnBadRequest() {

        EmailChangeNotAllowedException exception =
                new EmailChangeNotAllowedException(
                        "El correo no puede modificarse directamente"
                );

        HttpServletRequest request =
                createRequest(
                        "PUT",
                        "/users/3"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleEmailChangeNotAllowed(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                400,
                body.getStatus()
        );

        assertEquals(
                "EMAIL CHANGE NOT ALLOWED",
                body.getError()
        );

        assertEquals(
                "El correo no puede modificarse directamente",
                body.getMessage()
        );

        assertEquals(
                "/users/3",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }
    @Test
    void handleValidationErrors_shouldReturnBadRequestWithFieldErrors() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError emailError =
                new FieldError(
                        "userRequestDTO",
                        "email",
                        "El correo no es válido"
                );

        FieldError nameError =
                new FieldError(
                        "userRequestDTO",
                        "name",
                        "El nombre es obligatorio"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                emailError,
                                nameError
                        )
                );

        HttpServletRequest request =
                createRequest(
                        "POST",
                        "/users"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleValidationErrors(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                400,
                body.getStatus()
        );

        assertEquals(
                "VALIDATION ERROR",
                body.getError()
        );

        assertEquals(
                "/users",
                body.getPath()
        );

        Map<String, String> validationErrors =
                body.getValidationErrors();

        assertNotNull(validationErrors);

        assertEquals(
                2,
                validationErrors.size()
        );

        assertEquals(
                "El correo no es válido",
                validationErrors.get("email")
        );

        assertEquals(
                "El nombre es obligatorio",
                validationErrors.get("name")
        );
    }

    @Test
    void handleInvalidJson_shouldReturnBadRequest() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        HttpServletRequest request =
                createRequest(
                        "POST",
                        "/users"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidJson(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                400,
                body.getStatus()
        );

        assertEquals(
                "INVALID JSON",
                body.getError()
        );

        assertEquals(
                "/users",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Conflicto de datos"
                );

        HttpServletRequest request =
                createRequest(
                        "POST",
                        "/users"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleDataIntegrityViolation(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                409,
                body.getStatus()
        );

        assertEquals(
                "DATA INTEGRITY ERROR",
                body.getError()
        );

        assertEquals(
                "/users",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {

        Exception exception =
                new RuntimeException(
                        "Error inesperado"
                );

        HttpServletRequest request =
                createRequest(
                        "GET",
                        "/users"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGeneralException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                500,
                body.getStatus()
        );

        assertEquals(
                "INTERNAL SERVER ERROR",
                body.getError()
        );

        assertEquals(
                "/users",
                body.getPath()
        );

        assertNull(
                body.getValidationErrors()
        );
    }
    private MockHttpServletRequest createRequest(
            String method,
            String path
    ) {

        return new MockHttpServletRequest(
                method,
                path
        );
    }
}
