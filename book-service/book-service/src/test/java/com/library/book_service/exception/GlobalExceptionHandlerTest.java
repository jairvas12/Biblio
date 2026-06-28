package com.library.book_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {

        exceptionHandler =
                new GlobalExceptionHandler();

        request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/books/99"
        );
    }

    @Test
    void handleResourceNotFoundShouldReturnNotFound() {

        // Given
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "No existe un libro con id 99"
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleResourceNotFound(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleBusinessExceptionShouldReturnBadRequest() {

        // Given
        BusinessException exception =
                new BusinessException(
                        "El ISBN ya se encuentra registrado"
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleBusinessException(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleRemoteServiceExceptionShouldReturnServiceUnavailable() {

        // Given
        RemoteServiceException exception =
                new RemoteServiceException(
                        "No fue posible comunicarse con CATEGORY"
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleRemoteServiceException(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleValidationExceptionShouldReturnBadRequest() {

        // Given
        MethodArgumentNotValidException exception =
                mock(
                        MethodArgumentNotValidException.class
                );

        BindingResult bindingResult =
                mock(
                        BindingResult.class
                );

        FieldError fieldError =
                new FieldError(
                        "bookRequestDTO",
                        "title",
                        "El título es obligatorio"
                );

        when(
                exception.getBindingResult()
        ).thenReturn(
                bindingResult
        );

        when(
                bindingResult.getFieldErrors()
        ).thenReturn(
                List.of(fieldError)
        );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleValidationException(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleUnreadableMessageShouldReturnBadRequest() {

        // Given
        HttpMessageNotReadableException exception =
                mock(
                        HttpMessageNotReadableException.class
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleUnreadableMessage(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleDataIntegrityViolationShouldReturnConflict() {

        // Given
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Conflicto de datos"
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDataIntegrityViolation(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }

    @Test
    void handleGeneralExceptionShouldReturnInternalServerError() {

        // Given
        Exception exception =
                new Exception(
                        "Error inesperado"
                );

        // When
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleGeneralException(
                        exception,
                        request
                );

        // Then
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );
    }
}


