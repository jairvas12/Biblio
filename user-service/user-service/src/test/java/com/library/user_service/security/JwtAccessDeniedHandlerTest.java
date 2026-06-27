package com.library.user_service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;

    private JwtAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        accessDeniedHandler =
                new JwtAccessDeniedHandler(
                        objectMapper
                );
    }

    @Test
    void handle_shouldReturnForbiddenJsonResponse()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "DELETE",
                        "/users/3"
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

        JsonNode responseBody =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertNotNull(
                responseBody.get("timestamp")
        );

        assertEquals(
                403,
                responseBody.get("status").asInt()
        );

        assertEquals(
                "ACCESS DENIED",
                responseBody.get("error").asText()
        );

        assertEquals(
                "No posee permisos para realizar esta operación",
                responseBody.get("message").asText()
        );

        assertEquals(
                "/users/3",
                responseBody.get("path").asText()
        );
    }
}


