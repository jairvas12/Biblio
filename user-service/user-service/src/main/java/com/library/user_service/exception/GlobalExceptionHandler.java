package com.library.user_service.exception;

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

    @ExceptionHandler(
            EmailChangeNotAllowedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleEmailChangeNotAllowed(
            EmailChangeNotAllowedException ex,
            HttpServletRequest request
    ) {

    ErrorResponse error = buildError(
            HttpStatus.BAD_REQUEST,
            "EMAIL CHANGE NOT ALLOWED",
            ex.getMessage(),
            request.getRequestURI(),
            null
    );

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = buildError(
                HttpStatus.NOT_FOUND,
                "USER NOT FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = buildError(
                HttpStatus.CONFLICT,
                "EMAIL ALREADY EXISTS",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(
            InvalidRoleException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID ROLE",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        validationErrors.put(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ErrorResponse error = buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION ERROR",
                "Existen datos inválidos en la solicitud",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID JSON",
                "El cuerpo de la solicitud no es válido o está mal formado",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Data integrity violation in path {}",
                request.getRequestURI()
        );

        ErrorResponse error = buildError(
                HttpStatus.CONFLICT,
                "DATA INTEGRITY ERROR",
                "La operación genera un conflicto con los datos existentes",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected error in path {}",
                request.getRequestURI(),
                ex
        );

        ErrorResponse error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL SERVER ERROR",
                "Ocurrió un error interno inesperado",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    private ErrorResponse buildError(
            HttpStatus status,
            String errorName,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorName)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}