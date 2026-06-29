package com.library.security_service.controller;

import com.library.security_service.dto.SecurityEventRequestDTO;
import com.library.security_service.dto.SecurityEventResponseDTO;
import com.library.security_service.model.SecurityEventType;
import com.library.security_service.service.SecurityEventService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityEventControllerTest {

    @Mock
    private SecurityEventService service;

    @InjectMocks
    private SecurityEventController controller;

    private SecurityEventRequestDTO request;
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

        response = new SecurityEventResponseDTO(
                1L,
                10L,
                "usuario@biblio.cl",
                SecurityEventType.LOGIN_SUCCESS,
                "Inicio de sesión correcto",
                "127.0.0.1",
                true,
                createdAt
        );
    }

    @Test
    void createDebeRetornar201() {

        when(service.create(request))
                .thenReturn(response);

        ResponseEntity<SecurityEventResponseDTO> result =
                controller.create(request);

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode()
        );

        assertEquals(
                response,
                result.getBody()
        );

        verify(service).create(request);
    }

    @Test
    void findAllDebeRetornarLista() {

        when(service.findAll())
                .thenReturn(List.of(response));

        ResponseEntity<List<SecurityEventResponseDTO>> result =
                controller.findAll();

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(response, result.getBody().get(0));

        verify(service).findAll();
    }

    @Test
    void findByIdDebeRetornar200() {

        when(service.findById(1L))
                .thenReturn(response);

        ResponseEntity<SecurityEventResponseDTO> result =
                controller.findById(1L);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertEquals(
                response,
                result.getBody()
        );

        verify(service).findById(1L);
    }

    @Test
    void findByUserIdDebeRetornarEventos() {

        when(service.findByUserId(10L))
                .thenReturn(List.of(response));

        ResponseEntity<List<SecurityEventResponseDTO>> result =
                controller.findByUserId(10L);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        verify(service).findByUserId(10L);
    }

    @Test
    void findByTypeDebeRetornarEventos() {

        when(
                service.findByType(
                        SecurityEventType.LOGIN_SUCCESS
                )
        ).thenReturn(List.of(response));

        ResponseEntity<List<SecurityEventResponseDTO>> result =
                controller.findByType(
                        SecurityEventType.LOGIN_SUCCESS
                );

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        verify(service).findByType(
                SecurityEventType.LOGIN_SUCCESS
        );
    }

    @Test
    void deleteDebeRetornar204() {

        doNothing()
                .when(service)
                .delete(1L);

        ResponseEntity<Void> result =
                controller.delete(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                result.getStatusCode()
        );

        assertNull(result.getBody());

        verify(service).delete(1L);
    }
}