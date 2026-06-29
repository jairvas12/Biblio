package com.library.security_service.service;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.exception.BusinessException;
import com.library.security_service.exception.ResourceNotFoundException;
import com.library.security_service.mapper.SecurityEventMapper;
import com.library.security_service.model.SecurityEvent;
import com.library.security_service.model.SecurityEventType;
import com.library.security_service.repository.SecurityEventRepository;
import com.library.security_service.service.impl.SecurityEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityEventServiceImplTest {

    @Mock
    private SecurityEventRepository repository;

    @Mock
    private SecurityEventMapper mapper;

    @InjectMocks
    private SecurityEventServiceImpl service;

    private SecurityEventRequestDTO request;
    private SecurityEvent event;
    private SecurityEventResponseDTO response;

    @BeforeEach
    void setUp() {

        LocalDateTime createdAt = LocalDateTime.now();

        request = new SecurityEventRequestDTO(
                10L,
                "usuario@biblio.cl",
                SecurityEventType.LOGIN_SUCCESS,
                "Inicio de sesión correcto",
                "127.0.0.1",
                true
        );

        event = SecurityEvent.builder()
                .id(1L)
                .userId(10L)
                .username(request.username())
                .type(request.type())
                .description(request.description())
                .ipAddress(request.ipAddress())
                .successful(true)
                .createdAt(createdAt)
                .build();

        response = new SecurityEventResponseDTO(
                1L,
                10L,
                request.username(),
                request.type(),
                request.description(),
                request.ipAddress(),
                true,
                createdAt
        );
    }

    @Test
    void createDebeGuardarEventoCorrectamente() {

        when(mapper.toEntity(request))
                .thenReturn(event);

        when(repository.save(event))
                .thenReturn(event);

        when(mapper.toResponse(event))
                .thenReturn(response);

        SecurityEventResponseDTO result =
                service.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(mapper).toEntity(request);
        verify(repository).save(event);
        verify(mapper).toResponse(event);
    }

    @Test
    void createDebeRechazarSolicitudNull() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(null)
        );

        assertEquals(
                "La solicitud del evento de seguridad no puede ser nula",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void createDebeRechazarResultadoNull() {

        SecurityEventRequestDTO invalidRequest =
                new SecurityEventRequestDTO(
                        10L,
                        "usuario@biblio.cl",
                        SecurityEventType.LOGIN_FAILURE,
                        "Intento de inicio de sesión",
                        "127.0.0.1",
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(invalidRequest)
        );

        assertEquals(
                "El resultado del evento de seguridad es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void findAllDebeRetornarEventosOrdenados() {

        when(repository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(event));

        when(mapper.toResponse(event))
                .thenReturn(response);

        List<SecurityEventResponseDTO> result =
                service.findAll();

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(repository)
                .findAllByOrderByCreatedAtDesc();

        verify(mapper)
                .toResponse(event);
    }

    @Test
    void findByIdDebeRetornarEventoExistente() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(event));

        when(mapper.toResponse(event))
                .thenReturn(response);

        SecurityEventResponseDTO result =
                service.findById(1L);

        assertEquals(response, result);

        verify(repository).findById(1L);
        verify(mapper).toResponse(event);
    }

    @Test
    void findByIdDebeRechazarIdCero() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findById(0L)
        );

        assertEquals(
                "El ID del evento de seguridad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void findByIdDebeRechazarIdNull() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findById(null)
        );

        assertEquals(
                "El ID del evento de seguridad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(99L)
        );

        assertEquals(
                "No se encontró el evento de seguridad con ID 99",
                exception.getMessage()
        );

        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByUserIdDebeRetornarEventosDelUsuario() {

        when(
                repository
                        .findByUserIdOrderByCreatedAtDesc(10L)
        ).thenReturn(List.of(event));

        when(mapper.toResponse(event))
                .thenReturn(response);

        List<SecurityEventResponseDTO> result =
                service.findByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(repository)
                .findByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void findByUserIdDebeRechazarIdInvalido() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findByUserId(-1L)
        );

        assertEquals(
                "El ID del usuario debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void findByTypeDebeRetornarEventosDelTipo() {

        when(
                repository.findByTypeOrderByCreatedAtDesc(
                        SecurityEventType.LOGIN_SUCCESS
                )
        ).thenReturn(List.of(event));

        when(mapper.toResponse(event))
                .thenReturn(response);

        List<SecurityEventResponseDTO> result =
                service.findByType(
                        SecurityEventType.LOGIN_SUCCESS
                );

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(repository)
                .findByTypeOrderByCreatedAtDesc(
                        SecurityEventType.LOGIN_SUCCESS
                );
    }

    @Test
    void findByTypeDebeRechazarTipoNull() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findByType(null)
        );

        assertEquals(
                "El tipo de evento de seguridad es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void deleteDebeEliminarEventoExistente() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(event));

        service.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(event);
    }
}