package com.library.find_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith(
                                "Bearer "
                        )
        ) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (token.isBlank()) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {
            Claims claims =
                    jwtService
                            .validateAndExtractClaims(
                                    token
                            );

            String email =
                    claims.getSubject();

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            if (
                    email != null
                            && !email.isBlank()
                            && role != null
                            && !role.isBlank()
                            && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null
            ) {
                String authorityName =
                        role.startsWith("ROLE_")
                                ? role.toUpperCase(
                                        Locale.ROOT
                                )
                                : "ROLE_"
                                + role.toUpperCase(
                                        Locale.ROOT
                                );

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                authorityName
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(
                                        request
                                )
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }

        } catch (
                JwtException
                        | IllegalArgumentException exception
        ) {
            SecurityContextHolder.clearContext();

            log.warn(
                    "JWT inválido en la ruta {}: {}",
                    request.getRequestURI(),
                    exception.getMessage()
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}