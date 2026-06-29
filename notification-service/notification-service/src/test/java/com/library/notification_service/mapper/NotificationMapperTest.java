package com.library.notification_service.mapper;

import com.library.notification_service.dto.NotificationRequestDTO;
import com.library.notification_service.dto.NotificationResponseDTO;
import com.library.notification_service.model.Notification;
import com.library.notification_service.model.TipoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void toEntityDebeConvertirSolicitudCorrectamente() {

        NotificationRequestDTO request =
                new NotificationRequestDTO(
                        10L,
                        "Préstamo próximo a vencer",
                        "Debe devolver el libro mañana",
                        TipoNotificacion.PRESTAMO
                );

        Notification result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals(
                "Préstamo próximo a vencer",
                result.getTitle()
        );
        assertEquals(
                "Debe devolver el libro mañana",
                result.getMessage()
        );
        assertEquals(
                TipoNotificacion.PRESTAMO,
                result.getType()
        );
        assertFalse(result.isRead());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void toEntityDebeRetornarNullCuandoSolicitudEsNull() {

        Notification result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toResponseDebeConvertirEntidadCorrectamente() {

        LocalDateTime createdAt = LocalDateTime.now();

        Notification notification = Notification.builder()
                .id(1L)
                .userId(10L)
                .title("Reserva disponible")
                .message("El libro reservado está disponible")
                .type(TipoNotificacion.RESERVA)
                .read(true)
                .createdAt(createdAt)
                .build();

        NotificationResponseDTO result =
                mapper.toResponse(notification);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.userId());
        assertEquals("Reserva disponible", result.title());
        assertEquals(
                "El libro reservado está disponible",
                result.message()
        );
        assertEquals(
                TipoNotificacion.RESERVA,
                result.type()
        );
        assertTrue(result.read());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void toResponseDebeRetornarNullCuandoEntidadEsNull() {

        NotificationResponseDTO result =
                mapper.toResponse(null);

        assertNull(result);
    }
}