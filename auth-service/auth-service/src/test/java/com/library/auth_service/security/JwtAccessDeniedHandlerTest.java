package com.library.auth_service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;

    private JwtAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {

        objectMapper =
                new ObjectMapper();

        accessDeniedHandler =
                new JwtAccessDeniedHandler(
                        objectMapper
                );
    }

    @Test
    void handle_shouldReturnForbiddenJsonResponse()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/auth/admin/users/3/email"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException(
                        "Acceso denegado"
                );

        accessDeniedHandler.handle(
                request,
                response,
                exception
        );

        assertEquals(
                403,
                response.getStatus()
        );

        assertTrue(
                response.getContentType()
                        .startsWith(
                                "application/json"
                        )
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );

        Map<String, Object> responseBody =
                objectMapper.readValue(
                        response.getContentAsString(),
                        new TypeReference<Map<String, Object>>() {
                        }
                );

        assertNotNull(
                responseBody.get("timestamp")
        );

        assertEquals(
                403,
                responseBody.get("status")
        );

        assertEquals(
                "FORBIDDEN",
                responseBody.get("error")
        );

        assertEquals(
                "No tiene permisos para acceder a este recurso",
                responseBody.get("message")
        );

        assertEquals(
                "/auth/admin/users/3/email",
                responseBody.get("path")
        );
    }
}
