package com.library.security_service.mapper;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.model.SecurityEvent;
import com.library.security_service.model.SecurityEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SecurityEventMapperTest {

    private SecurityEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SecurityEventMapper();
    }

    @Test
    void toEntityDebeConvertirSolicitudCorrectamente() {

        SecurityEventRequestDTO request =
                new SecurityEventRequestDTO(
                        10L,
                        "usuario@biblio.cl",
                        SecurityEventType.LOGIN_SUCCESS,
                        "Inicio de sesión correcto",
                        "127.0.0.1",
                        true
                );

        SecurityEvent result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals(
                "usuario@biblio.cl",
                result.getUsername()
        );
        assertEquals(
                SecurityEventType.LOGIN_SUCCESS,
                result.getType()
        );
        assertEquals(
                "Inicio de sesión correcto",
                result.getDescription()
        );
        assertEquals(
                "127.0.0.1",
                result.getIpAddress()
        );
        assertTrue(result.isSuccessful());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void toEntityDebeRetornarNullCuandoSolicitudEsNull() {

        SecurityEvent result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toResponseDebeConvertirEntidadCorrectamente() {

        LocalDateTime createdAt = LocalDateTime.now();

        SecurityEvent event = SecurityEvent.builder()
                .id(1L)
                .userId(10L)
                .username("usuario@biblio.cl")
                .type(SecurityEventType.ACCESS_DENIED)
                .description("Acceso denegado")
                .ipAddress("192.168.1.10")
                .successful(false)
                .createdAt(createdAt)
                .build();

        SecurityEventResponseDTO result =
                mapper.toResponse(event);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.userId());
        assertEquals(
                "usuario@biblio.cl",
                result.username()
        );
        assertEquals(
                SecurityEventType.ACCESS_DENIED,
                result.type()
        );
        assertEquals(
                "Acceso denegado",
                result.description()
        );
        assertEquals(
                "192.168.1.10",
                result.ipAddress()
        );
        assertFalse(result.successful());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void toResponseDebeRetornarNullCuandoEventoEsNull() {

        SecurityEventResponseDTO result =
                mapper.toResponse(null);

        assertNull(result);
    }
}