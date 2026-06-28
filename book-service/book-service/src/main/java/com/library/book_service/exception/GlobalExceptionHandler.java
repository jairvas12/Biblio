package com.library.book_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "NOT FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse>
    handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BUSINESS ERROR",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ApiErrorResponse>
    handleRemoteServiceException(
            RemoteServiceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "REMOTE SERVICE ERROR",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION ERROR",
                "Existen datos inválidos en la solicitud",
                request.getRequestURI(),
                validationErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse>
    handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID JSON",
                "El cuerpo de la solicitud no tiene un formato válido",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA INTEGRITY ERROR",
                "La operación genera un conflicto con los datos almacenados",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error interno no controlado en {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL SERVER ERROR",
                "Ocurrió un error interno en el servidor",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiErrorResponse>
    buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        error,
                        message,
                        path,
                        validationErrors
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}