package com.library.prestamo_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void manejarRecursoNoEncontrado_deberiaRetornarNotFound() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos/99"
        );

        RecursoNoEncontradoException exception =
                new RecursoNoEncontradoException(
                        "No se encontró el préstamo con ID 99"
                );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarRecursoNoEncontrado(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.NOT_FOUND,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertNotNull(cuerpo.getTimestamp()),
                () -> assertEquals(
                        404,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Not Found",
                        cuerpo.getError()
                ),
                () -> assertEquals(
                        "No se encontró el préstamo con ID 99",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos/99",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }

    @Test
    void manejarReglaNegocio_deberiaRetornarConflict() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos"
        );

        ReglaNegocioException exception =
                new ReglaNegocioException(
                        "La copia no está disponible"
                );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarReglaNegocio(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.CONFLICT,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        409,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Conflict",
                        cuerpo.getError()
                ),
                () -> assertEquals(
                        "La copia no está disponible",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }

    @Test
    void manejarServicioRemoto_deberiaRetornarServiceUnavailable() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos"
        );

        ServicioRemotoException exception =
                new ServicioRemotoException(
                        "No fue posible conectar con copia-service"
                );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarServicioRemoto(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        503,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Service Unavailable",
                        cuerpo.getError()
                ),
                () -> assertEquals(
                        "No fue posible conectar con copia-service",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }

    @Test
    void manejarErrorNoControlado_deberiaRetornarInternalServerError() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos/actualizar-atrasados"
        );

        Exception exception =
                new Exception(
                        "Error inesperado de prueba"
                );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarErrorNoControlado(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        500,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Internal Server Error",
                        cuerpo.getError()
                ),
                () -> assertEquals(
                        "Ocurrió un error interno inesperado",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos/actualizar-atrasados",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }

    @Test
    void manejarValidacionesDTO_deberiaRetornarBadRequestConErrores() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos"
        );

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError errorUsuario =
                new FieldError(
                        "crearPrestamoRequestDTO",
                        "usuarioId",
                        "El identificador del usuario es obligatorio"
                );

        FieldError errorCopia =
                new FieldError(
                        "crearPrestamoRequestDTO",
                        "copiaId",
                        "El identificador de la copia es obligatorio"
                );

        when(
                exception.getBindingResult()
        ).thenReturn(
                bindingResult
        );

        when(
                bindingResult.getFieldErrors()
        ).thenReturn(
                List.of(
                        errorUsuario,
                        errorCopia
                )
        );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarValidacionesDTO(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.BAD_REQUEST,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        400,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Bad Request",
                        cuerpo.getError()
                ),
                () -> assertEquals(
                        "Existen campos con errores de validación",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos",
                        cuerpo.getPath()
                ),
                () -> assertNotNull(
                        cuerpo.getValidationErrors()
                ),
                () -> assertEquals(
                        "El identificador del usuario es obligatorio",
                        cuerpo.getValidationErrors().get("usuarioId")
                ),
                () -> assertEquals(
                        "El identificador de la copia es obligatorio",
                        cuerpo.getValidationErrors().get("copiaId")
                )
        );
    }

    @Test
    void manejarValidacionesParametros_deberiaRetornarBadRequestConErrores() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos/0"
        );

        ConstraintViolation<?> violation =
                mock(ConstraintViolation.class);

        Path propertyPath =
                mock(Path.class);

        when(
                violation.getPropertyPath()
        ).thenReturn(
                propertyPath
        );

        when(
                propertyPath.toString()
        ).thenReturn(
                "obtenerPorId.prestamoId"
        );

        when(
                violation.getMessage()
        ).thenReturn(
                "El identificador del préstamo debe ser mayor que cero"
        );

        ConstraintViolationException exception =
                new ConstraintViolationException(
                        Set.of(violation)
                );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarValidacionesParametros(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.BAD_REQUEST,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        400,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "Existen parámetros con errores de validación",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos/0",
                        cuerpo.getPath()
                ),
                () -> assertEquals(
                        "El identificador del préstamo debe ser mayor que cero",
                        cuerpo.getValidationErrors().get(
                                "obtenerPorId.prestamoId"
                        )
                )
        );
    }

    @Test
    void manejarTipoParametroIncorrecto_deberiaRetornarBadRequest() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos/estado/INVALIDO"
        );

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(
                exception.getName()
        ).thenReturn(
                "estado"
        );

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarTipoParametroIncorrecto(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.BAD_REQUEST,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        400,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "El parámetro 'estado' contiene un valor no válido",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos/estado/INVALIDO",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }

    @Test
    void manejarJsonInvalido_deberiaRetornarBadRequest() {

        when(
                request.getRequestURI()
        ).thenReturn(
                "/prestamos"
        );

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiErrorResponse> respuesta =
                exceptionHandler.manejarJsonInvalido(
                        exception,
                        request
                );

        ApiErrorResponse cuerpo =
                respuesta.getBody();

        assertAll(
                () -> assertEquals(
                        HttpStatus.BAD_REQUEST,
                        respuesta.getStatusCode()
                ),
                () -> assertNotNull(cuerpo),
                () -> assertEquals(
                        400,
                        cuerpo.getStatus()
                ),
                () -> assertEquals(
                        "El cuerpo de la solicitud contiene datos inválidos o mal formados",
                        cuerpo.getMessage()
                ),
                () -> assertEquals(
                        "/prestamos",
                        cuerpo.getPath()
                ),
                () -> assertNull(
                        cuerpo.getValidationErrors()
                )
        );
    }


}
