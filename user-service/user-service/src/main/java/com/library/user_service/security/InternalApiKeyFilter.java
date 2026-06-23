package com.library.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter
        extends OncePerRequestFilter {

    public static final String INTERNAL_HEADER =
            "X-Internal-API-Key";

    private final ObjectMapper objectMapper;

    @Value("${internal.api.key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String receivedApiKey =
                request.getHeader(INTERNAL_HEADER);

        if (!isValidApiKey(receivedApiKey)) {

            sendUnauthorizedResponse(
                    request,
                    response
            );

            return;
        }

        request.setAttribute(
                "internalRequest",
                true
        );

        filterChain.doFilter(
                request,
                response
        );
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        String method =
                request.getMethod();

        boolean createUser =
                method.equalsIgnoreCase("POST")
                        && path.equals("/users");

        boolean findByEmail =
                method.equalsIgnoreCase("GET")
                        && path.startsWith(
                                "/users/email/"
                        );

        boolean compensateRegistration =
                method.equalsIgnoreCase("DELETE")
                        && path.matches(
                                "^/users/internal/\\d+/deactivate$"
                        );
        boolean updateEmailInternally =
                method.equalsIgnoreCase("PUT")
                        && path.matches(
                                "^/users/internal/\\d+/email$"
                        );

        return !(
                createUser
                || findByEmail
                || compensateRegistration
                || updateEmailInternally
        );
    }

    private boolean isValidApiKey(
            String receivedApiKey
    ) {

        if (receivedApiKey == null
                || receivedApiKey.isBlank()) {

            return false;
        }

        byte[] expectedBytes =
                expectedApiKey.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] receivedBytes =
                receivedApiKey.getBytes(
                        StandardCharsets.UTF_8
                );

        return MessageDigest.isEqual(
                expectedBytes,
                receivedBytes
        );
    }

    private void sendUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding("UTF-8");

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
                "INVALID INTERNAL CREDENTIAL"
        );

        error.put(
                "message",
                "La solicitud interna no posee una credencial válida"
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
