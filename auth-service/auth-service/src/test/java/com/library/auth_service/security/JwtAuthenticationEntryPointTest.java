package com.library.auth_service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;

    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {

        objectMapper =
                new ObjectMapper();

        entryPoint =
                new JwtAuthenticationEntryPoint(
                        objectMapper
                );
    }

    @Test
    void commence_shouldReturnUnauthorizedJsonResponse()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/auth/admin/users/10/email"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException(
                        "Token JWT inválido o expirado"
                );

        entryPoint.commence(
                request,
                response,
                exception
        );

        assertEquals(
                401,
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
                401,
                responseBody.get("status")
        );

        assertEquals(
                "UNAUTHORIZED",
                responseBody.get("error")
        );

        assertEquals(
                "Token JWT inválido o expirado",
                responseBody.get("message")
        );

        assertEquals(
                "/auth/admin/users/10/email",
                responseBody.get("path")
        );
    }
}
