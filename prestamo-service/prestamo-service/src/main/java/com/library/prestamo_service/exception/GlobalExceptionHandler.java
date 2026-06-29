package com.library.prestamo_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Recurso no encontrado en {}: {}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return construirRespuesta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorResponse> manejarReglaNegocio(
            ReglaNegocioException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Regla de negocio incumplida en {}: {}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return construirRespuesta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(ServicioRemotoException.class)
    public ResponseEntity<ApiErrorResponse> manejarServicioRemoto(
            ServicioRemotoException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error de comunicación remota en {}: {}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return construirRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacionesDTO(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            errores.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        log.warn(
                "Validación fallida en {}: {}",
                request.getRequestURI(),
                errores
        );

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Existen campos con errores de validación",
                request.getRequestURI(),
                errores
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacionesParametros(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation :
                exception.getConstraintViolations()) {

            errores.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            );
        }

        log.warn(
                "Validación de parámetros fallida en {}: {}",
                request.getRequestURI(),
                errores
        );

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Existen parámetros con errores de validación",
                request.getRequestURI(),
                errores
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoParametroIncorrecto(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String mensaje =
                "El parámetro '" +
                exception.getName() +
                "' contiene un valor no válido";

        log.warn(
                "Tipo de parámetro incorrecto en {}: {}",
                request.getRequestURI(),
                mensaje
        );

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Cuerpo JSON inválido en {}",
                request.getRequestURI()
        );

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud contiene datos inválidos o mal formados",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorNoControlado(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error interno no controlado en {}",
                request.getRequestURI(),
                exception
        );

        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> construirRespuesta(
            HttpStatus status,
            String mensaje,
            String path,
            Map<String, String> erroresValidacion
    ) {
        ApiErrorResponse respuesta = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(mensaje)
                .path(path)
                .validationErrors(erroresValidacion)
                .build();

        return ResponseEntity
                .status(status)
                .body(respuesta);
    }
}