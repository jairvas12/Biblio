package com.library.find_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import org.springframework.security.core.AuthenticationException;

import org.springframework.security.web.AuthenticationEntryPoint;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        this.objectMapper =
                objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException, ServletException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        Map<String, Object> error =
                new LinkedHashMap<>();

        error.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        error.put(
                "status",
                HttpServletResponse.SC_UNAUTHORIZED
        );

        error.put(
                "error",
                "UNAUTHORIZED"
        );

        error.put(
                "message",
                authenticationException.getMessage()
        );

        error.put(
                "path",
                request.getRequestURI()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}