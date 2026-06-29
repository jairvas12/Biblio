package com.library.notification_service.controller;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.model.TipoNotificacion;
import com.library.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationController controller;

    private NotificationRequestDTO request;
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
    void createDebeRetornar201() {

        when(service.create(request))
                .thenReturn(response);

        ResponseEntity<NotificationResponseDTO> result =
                controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());

        verify(service).create(request);
    }

    @Test
    void findByIdDebeRetornar200() {

        when(service.findById(1L))
                .thenReturn(response);

        ResponseEntity<NotificationResponseDTO> result =
                controller.findById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());

        verify(service).findById(1L);
    }

    @Test
    void findByUserIdDebeRetornarLista() {

        when(service.findByUserId(10L))
                .thenReturn(List.of(response));

        ResponseEntity<List<NotificationResponseDTO>> result =
                controller.findByUserId(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(response, result.getBody().get(0));

        verify(service).findByUserId(10L);
    }

    @Test
    void findUnreadByUserIdDebeRetornarLista() {

        when(service.findUnreadByUserId(10L))
                .thenReturn(List.of(response));

        ResponseEntity<List<NotificationResponseDTO>> result =
                controller.findUnreadByUserId(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        verify(service).findUnreadByUserId(10L);
    }

    @Test
    void countUnreadByUserIdDebeRetornarCantidad() {

        when(service.countUnreadByUserId(10L))
                .thenReturn(4L);

        ResponseEntity<Map<String, Long>> result =
                controller.countUnreadByUserId(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(4L, result.getBody().get("unreadCount"));

        verify(service).countUnreadByUserId(10L);
    }

    @Test
    void markAsReadDebeRetornarNotificacionActualizada() {

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

        when(service.markAsRead(1L))
                .thenReturn(readResponse);

        ResponseEntity<NotificationResponseDTO> result =
                controller.markAsRead(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().read());

        verify(service).markAsRead(1L);
    }

    @Test
    void deleteDebeRetornar204() {

        doNothing()
                .when(service)
                .delete(1L);

        ResponseEntity<Void> result =
                controller.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());

        verify(service).delete(1L);
    }
}