package com.library.auth_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler =
                new GlobalExceptionHandler();

        request =
                mock(HttpServletRequest.class);

        when(
                request.getRequestURI()
        ).thenReturn("/auth/test");
    }

    @Test
    void handleCredentialNotFound_shouldReturnNotFound() {

        CredentialNotFoundException exception =
                new CredentialNotFoundException(
                        "Credencial no encontrada"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleCredentialNotFound(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "CREDENTIAL NOT FOUND",
                "Credencial no encontrada"
        );
    }

    @Test
    void handleEmailChangeProcess_shouldReturnInternalServerError() {

        EmailChangeProcessException exception =
                new EmailChangeProcessException(
                        "Falló el cambio de correo"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleEmailChangeProcess(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "EMAIL CHANGE ERROR",
                "Falló el cambio de correo"
        );
    }

    @Test
    void handleInvalidCredentials_shouldReturnUnauthorized() {

        InvalidCredentialsException exception =
                new InvalidCredentialsException(
                        "Credenciales incorrectas"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "INVALID CREDENTIALS",
                "Credenciales incorrectas"
        );
    }

    @Test
    void handleEmailAlreadyRegistered_shouldReturnConflict() {

        EmailAlreadyRegisteredException exception =
                new EmailAlreadyRegisteredException(
                        "Correo ya registrado"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleEmailAlreadyRegistered(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "EMAIL ALREADY REGISTERED",
                "Correo ya registrado"
        );
    }

    @Test
    void handleUserInactive_shouldReturnForbidden() {

        UserInactiveException exception =
                new UserInactiveException(
                        "Usuario inactivo"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUserInactive(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "USER INACTIVE",
                "Usuario inactivo"
        );
    }

    @Test
    void handleUserServiceUnavailable_shouldReturnServiceUnavailable() {

        UserServiceUnavailableException exception =
                new UserServiceUnavailableException(
                        "USER no disponible"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUserServiceUnavailable(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.SERVICE_UNAVAILABLE,
                "USER SERVICE UNAVAILABLE",
                "USER no disponible"
        );
    }

    @Test
    void handleRegistrationProcess_shouldReturnInternalServerError() {

        RegistrationProcessException exception =
                new RegistrationProcessException(
                        "Falló el registro"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleRegistrationProcess(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "REGISTRATION ERROR",
                "Falló el registro"
        );
    }

    @Test
    void handleValidationErrors_shouldReturnFieldErrors() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        List<FieldError> fieldErrors =
                List.of(
                        new FieldError(
                                "registerRequestDTO",
                                "email",
                                "El correo no es válido"
                        ),
                        new FieldError(
                                "registerRequestDTO",
                                "password",
                                "La contraseña es obligatoria"
                        )
                );

        when(
                exception.getBindingResult()
        ).thenReturn(bindingResult);

        when(
                bindingResult.getFieldErrors()
        ).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationErrors(
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
                "/auth/test",
                body.getPath()
        );

        assertNotNull(
                body.getTimestamp()
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
                "La contraseña es obligatoria",
                validationErrors.get("password")
        );
    }

    @Test
    void handleInvalidJson_shouldReturnBadRequest() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidJson(
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
                "/auth/test",
                body.getPath()
        );

        assertNotNull(
                body.getMessage()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Correo duplicado"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(
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
                "/auth/test",
                body.getPath()
        );

        assertNotNull(
                body.getMessage()
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

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneralException(
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
                "/auth/test",
                body.getPath()
        );

        assertNotNull(
                body.getMessage()
        );

        assertNull(
                body.getValidationErrors()
        );
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedError,
            String expectedMessage
    ) {

        assertEquals(
                expectedStatus,
                response.getStatusCode()
        );

        ErrorResponse body =
                response.getBody();

        assertNotNull(body);

        assertEquals(
                expectedStatus.value(),
                body.getStatus()
        );

        assertEquals(
                expectedError,
                body.getError()
        );

        assertEquals(
                expectedMessage,
                body.getMessage()
        );

        assertEquals(
                "/auth/test",
                body.getPath()
        );

        assertNotNull(
                body.getTimestamp()
        );

        assertNull(
                body.getValidationErrors()
        );
    }
}