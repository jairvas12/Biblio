package com.library.category_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        "NOT FOUND",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse>
    handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        "BUSINESS ERROR",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
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

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        "VALIDATION ERROR",
                        "Existen datos inválidos en la solicitud",
                        request.getRequestURI(),
                        validationErrors
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                HttpStatus.INTERNAL_SERVER_ERROR;

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        "INTERNAL SERVER ERROR",
                        "Ocurrió un error interno en el servidor",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}