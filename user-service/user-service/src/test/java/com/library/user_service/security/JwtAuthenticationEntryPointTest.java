package com.library.user_service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;

    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        entryPoint =
                new JwtAuthenticationEntryPoint(
                        objectMapper
                );
    }

    @Test
    void commence_shouldReturnUnauthorizedJsonResponse()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException(
                        "Token JWT inválido"
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
                    .startsWith("application/json") );

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
                401,
                responseBody.get("status").asInt()
        );

        assertEquals(
                "UNAUTHORIZED",
                responseBody.get("error").asText()
        );

        assertEquals(
                "Token JWT inválido",
                responseBody.get("message").asText()
        );

        assertEquals(
                "/users",
                responseBody.get("path").asText()
        );
    }
}


