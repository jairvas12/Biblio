
package com.library.copia_service.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        log.warn(
                "Recurso no encontrado en {}: {}",
                obtenerRuta(request),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex,
            WebRequest request
    ) {
        log.warn(
                "Regla de negocio incumplida en {}: {}",
                obtenerRuta(request),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ErrorResponse> handleRemoteService(
            RemoteServiceException ex,
            WebRequest request
    ) {
        log.error(
                "Error de comunicación remota en {}: {}",
                obtenerRuta(request),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        String errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        log.warn(
                "Validación fallida en {}: {}",
                obtenerRuta(request),
                errores
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                errores,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request
    ) {
        String errores = ex.getConstraintViolations()
                .stream()
                .map(violacion ->
                        violacion.getPropertyPath()
                                + ": "
                                + violacion.getMessage()
                )
                .collect(Collectors.joining(", "));

        log.warn(
                "Validación de parámetros fallida en {}: {}",
                obtenerRuta(request),
                errores
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                errores,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request
    ) {
        String mensaje =
                "El parámetro '"
                        + ex.getName()
                        + "' contiene un valor no válido";

        log.warn(
                "Tipo de parámetro incorrecto en {}: {}",
                obtenerRuta(request),
                mensaje
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            WebRequest request
    ) {
        String mensaje =
                "El cuerpo de la solicitud contiene datos inválidos";

        log.warn(
                "Cuerpo JSON inválido en {}",
                obtenerRuta(request)
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            WebRequest request
    ) {
        String mensaje = "La ruta solicitada no existe";

        log.warn(
                "Ruta no encontrada en {}: {}",
                obtenerRuta(request),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                mensaje,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            WebRequest request
    ) {
        log.error(
                "Error interno no controlado en {}",
                obtenerRuta(request),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            WebRequest request
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(obtenerRuta(request))
                .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private String obtenerRuta(WebRequest request) {
        return request
                .getDescription(false)
                .replace("uri=", "");
    }
}