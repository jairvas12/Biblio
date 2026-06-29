package com.library.copia_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest request;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();

        when(request.getDescription(false))
                .thenReturn("uri=/copias/99");
    }

    @Test
    void handleNotFoundDevuelve404() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "No existe una copia con ID: 99"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                404,
                response.getBody().getStatus()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "No existe una copia con ID: 99",
                response.getBody().getMessage()
        );

        assertEquals(
                "/copias/99",
                response.getBody().getPath()
        );

        assertNotNull(
                response.getBody().getTimestamp()
        );
    }

    @Test
    void handleBusinessDevuelve400() {
        BusinessException exception =
                new BusinessException(
                        "La copia no puede eliminarse"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleBusiness(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );

        assertEquals(
                "La copia no puede eliminarse",
                response.getBody().getMessage()
        );

        assertEquals(
                "/copias/99",
                response.getBody().getPath()
        );

        assertNotNull(
                response.getBody().getTimestamp()
        );
    }
    @Test
void handleRemoteServiceDevuelve503() {
    RemoteServiceException exception =
            new RemoteServiceException(
                    "No fue posible comunicarse con book-service"
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleRemoteService(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.SERVICE_UNAVAILABLE,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            503,
            response.getBody().getStatus()
    );

    assertEquals(
            "Service Unavailable",
            response.getBody().getError()
    );

    assertEquals(
            "No fue posible comunicarse con book-service",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}

@Test
void handleGeneralDevuelve500() {
    RuntimeException exception =
            new RuntimeException(
                    "Error inesperado de prueba"
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleGeneral(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            500,
            response.getBody().getStatus()
    );

    assertEquals(
            "Internal Server Error",
            response.getBody().getError()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}
@Test
void handleTypeMismatchDevuelve400() {
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception =
            org.mockito.Mockito.mock(
                    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
            );

    when(exception.getName())
            .thenReturn("estado");

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleTypeMismatch(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            400,
            response.getBody().getStatus()
    );

    assertEquals(
            "Bad Request",
            response.getBody().getError()
    );

    assertEquals(
            "El parámetro 'estado' contiene un valor no válido",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}

@Test
void handleUnreadableMessageDevuelve400() {
    org.springframework.http.converter.HttpMessageNotReadableException exception =
            org.mockito.Mockito.mock(
                    org.springframework.http.converter.HttpMessageNotReadableException.class
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleUnreadableMessage(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            400,
            response.getBody().getStatus()
    );

    assertEquals(
            "Bad Request",
            response.getBody().getError()
    );

    assertEquals(
            "El cuerpo de la solicitud contiene datos inválidos",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}
@Test
void handleValidationDevuelve400() {
    org.springframework.web.bind.MethodArgumentNotValidException exception =
            org.mockito.Mockito.mock(
                    org.springframework.web.bind.MethodArgumentNotValidException.class
            );

    org.springframework.validation.BindingResult bindingResult =
            org.mockito.Mockito.mock(
                    org.springframework.validation.BindingResult.class
            );

    org.springframework.validation.FieldError fieldError =
            new org.springframework.validation.FieldError(
                    "copiaRequestDTO",
                    "codigoCopia",
                    "El código de copia es obligatorio"
            );

    when(exception.getBindingResult())
            .thenReturn(bindingResult);

    when(bindingResult.getFieldErrors())
            .thenReturn(
                    java.util.List.of(fieldError)
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleValidation(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            400,
            response.getBody().getStatus()
    );

    assertEquals(
            "Bad Request",
            response.getBody().getError()
    );

    assertEquals(
            "codigoCopia: El código de copia es obligatorio",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}

@Test
void handleConstraintViolationDevuelve400() {
    jakarta.validation.ConstraintViolation<?> violation =
            org.mockito.Mockito.mock(
                    jakarta.validation.ConstraintViolation.class
            );

    jakarta.validation.Path propertyPath =
            org.mockito.Mockito.mock(
                    jakarta.validation.Path.class
            );

    when(violation.getPropertyPath())
            .thenReturn(propertyPath);

    when(propertyPath.toString())
            .thenReturn("buscarCopiaPorId.id");

    when(violation.getMessage())
            .thenReturn("debe ser mayor que cero");

    jakarta.validation.ConstraintViolationException exception =
            new jakarta.validation.ConstraintViolationException(
                    java.util.Set.of(violation)
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleConstraintViolation(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            400,
            response.getBody().getStatus()
    );

    assertEquals(
            "Bad Request",
            response.getBody().getError()
    );

    assertEquals(
            "buscarCopiaPorId.id: debe ser mayor que cero",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}

@Test
void handleNoResourceFoundDevuelve404() {
    org.springframework.web.servlet.resource.NoResourceFoundException exception =
            new org.springframework.web.servlet.resource.NoResourceFoundException(
                    org.springframework.http.HttpMethod.DELETE,
                    "copias/"
            );

    ResponseEntity<ErrorResponse> response =
            exceptionHandler.handleNoResourceFound(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
            404,
            response.getBody().getStatus()
    );

    assertEquals(
            "Not Found",
            response.getBody().getError()
    );

    assertEquals(
            "La ruta solicitada no existe",
            response.getBody().getMessage()
    );

    assertEquals(
            "/copias/99",
            response.getBody().getPath()
    );
}
}