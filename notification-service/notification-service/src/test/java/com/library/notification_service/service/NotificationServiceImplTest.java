package com.library.notification_service.service;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.exception.BusinessException;
import com.library.notification_service.exception.ResourceNotFoundException;
import com.library.notification_service.mapper.NotificationMapper;
import com.library.notification_service.model.Notification;
import com.library.notification_service.model.TipoNotificacion;
import com.library.notification_service.repository.NotificationRepository;
import com.library.notification_service.service.impl.NotificationServiceImpl;
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
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private NotificationServiceImpl service;

    private NotificationRequestDTO request;
    private Notification notification;
    private NotificationResponseDTO response;

    @BeforeEach
    void setUp() {

        LocalDateTime createdAt = LocalDateTime.now();

        request = new NotificationRequestDTO(
                10L,
                "Préstamo próximo a vencer",
                "Debe devolver el libro mañana",
                TipoNotificacion.PRESTAMO
        );

        notification = Notification.builder()
                .id(1L)
                .userId(10L)
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .read(false)
                .createdAt(createdAt)
                .build();

        response = new NotificationResponseDTO(
                1L,
                10L,
                request.title(),
                request.message(),
                request.type(),
                false,
                createdAt
        );
    }

    @Test
    void createDebeGuardarNotificacionCorrectamente() {

        when(mapper.toEntity(request))
                .thenReturn(notification);

        when(repository.save(notification))
                .thenReturn(notification);

        when(mapper.toResponse(notification))
                .thenReturn(response);

        NotificationResponseDTO result =
                service.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(mapper).toEntity(request);
        verify(repository).save(notification);
        verify(mapper).toResponse(notification);
    }

    @Test
    void createDebeRechazarSolicitudNull() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(null)
        );

        assertEquals(
                "La solicitud de notificación no puede ser nula",
                exception.getMessage()
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void findByIdDebeRetornarNotificacionExistente() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(mapper.toResponse(notification))
                .thenReturn(response);

        NotificationResponseDTO result =
                service.findById(1L);

        assertEquals(response, result);

        verify(repository).findById(1L);
        verify(mapper).toResponse(notification);
    }

    @Test
    void findByIdDebeRechazarIdInvalido() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findById(0L)
        );

        assertEquals(
                "El ID de la notificación debe ser mayor que cero",
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
                "No se encontró la notificación con ID 99",
                exception.getMessage()
        );

        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByUserIdDebeRetornarLista() {

        when(repository.findByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));

        when(mapper.toResponse(notification))
                .thenReturn(response);

        List<NotificationResponseDTO> result =
                service.findByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(repository)
                .findByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void findUnreadByUserIdDebeRetornarNoLeidas() {

        when(
                repository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(List.of(notification));

        when(mapper.toResponse(notification))
                .thenReturn(response);

        List<NotificationResponseDTO> result =
                service.findUnreadByUserId(10L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).read());

        verify(repository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        10L
                );
    }

    @Test
    void countUnreadByUserIdDebeRetornarCantidad() {

        when(repository.countByUserIdAndReadFalse(10L))
                .thenReturn(3L);

        long result =
                service.countUnreadByUserId(10L);

        assertEquals(3L, result);

        verify(repository)
                .countByUserIdAndReadFalse(10L);
    }

    @Test
    void markAsReadDebeMarcarNotificacionNoLeida() {

        NotificationResponseDTO readResponse =
                new NotificationResponseDTO(
                        response.id(),
                        response.userId(),
                        response.title(),
                        response.message(),
                        response.type(),
                        true,
                        response.createdAt()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(repository.save(notification))
                .thenReturn(notification);

        when(mapper.toResponse(notification))
                .thenReturn(readResponse);

        NotificationResponseDTO result =
                service.markAsRead(1L);

        assertTrue(notification.isRead());
        assertTrue(result.read());

        verify(repository).save(notification);
    }

    @Test
    void markAsReadNoDebeGuardarCuandoYaEstaLeida() {

        notification.setRead(true);

        NotificationResponseDTO readResponse =
                new NotificationResponseDTO(
                        response.id(),
                        response.userId(),
                        response.title(),
                        response.message(),
                        response.type(),
                        true,
                        response.createdAt()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(mapper.toResponse(notification))
                .thenReturn(readResponse);

        NotificationResponseDTO result =
                service.markAsRead(1L);

        assertTrue(result.read());

        verify(repository, never())
                .save(any(Notification.class));
    }

    @Test
    void deleteDebeEliminarNotificacionExistente() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        service.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(notification);
    }
}