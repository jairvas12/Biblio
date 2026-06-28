package com.library.book_service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtSecurityHandlerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper =
                new ObjectMapper();
    }

    @Test
    void authenticationEntryPointShouldWriteUnauthorizedResponse()
            throws Exception {

        // Given
        JwtAuthenticationEntryPoint entryPoint =
                new JwtAuthenticationEntryPoint(
                        objectMapper
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/books"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BadCredentialsException exception =
                new BadCredentialsException(
                        "Token requerido"
                );

        // When
        entryPoint.commence(
                request,
                response,
                exception
        );

        // Then
        JsonNode body =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentType()
                        .startsWith(
                                MediaType.APPLICATION_JSON_VALUE
                        )
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );

        assertTrue(
                body.hasNonNull("timestamp")
        );

        assertEquals(
                401,
                body.get("status").asInt()
        );

        assertEquals(
                "UNAUTHORIZED",
                body.get("error").asText()
        );

        assertEquals(
                "Token requerido",
                body.get("message").asText()
        );

        assertEquals(
                "/books",
                body.get("path").asText()
        );
    }

    @Test
    void accessDeniedHandlerShouldWriteForbiddenResponse()
            throws Exception {

        // Given
        JwtAccessDeniedHandler deniedHandler =
                new JwtAccessDeniedHandler(
                        objectMapper
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/books/1"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException(
                        "Acceso denegado"
                );

        // When
        deniedHandler.handle(
                request,
                response,
                exception
        );

        // Then
        JsonNode body =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertEquals(
                403,
                response.getStatus()
        );

        assertTrue(
                response.getContentType()
                        .startsWith(
                                MediaType.APPLICATION_JSON_VALUE
                        )
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );

        assertTrue(
                body.hasNonNull("timestamp")
        );

        assertEquals(
                403,
                body.get("status").asInt()
        );

        assertEquals(
                "FORBIDDEN",
                body.get("error").asText()
        );

        assertEquals(
                "No tiene permisos para acceder a este recurso",
                body.get("message").asText()
        );

        assertEquals(
                "/books/1",
                body.get("path").asText()
        );
    }
}


