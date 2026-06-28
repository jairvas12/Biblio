package com.library.book_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            JwtAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;

        this.authenticationEntryPoint =
                authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        "Authorization"
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
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "El token JWT está vacío"
                    )
            );

            return;
        }

        try {
            Claims claims =
                    jwtService.validateAndExtractClaims(
                            token
                    );

            String email =
                    claims.getSubject();

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            Object userIdClaim =
                    claims.get(
                            "userId"
                    );

            if (
                    email == null
                            || email.isBlank()
                            || role == null
                            || role.isBlank()
                            || userIdClaim == null
            ) {
                throw new BadCredentialsException(
                        "El token JWT no contiene la información requerida"
                );
            }

            if (
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null
            ) {
                String normalizedRole =
                        role.trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + normalizedRole
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );

                request.setAttribute(
                        "authenticatedUserId",
                        Long.valueOf(
                                userIdClaim.toString()
                        )
                );

                request.setAttribute(
                        "authenticatedUserRole",
                        normalizedRole
                );
            }

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (
                JwtException
                | IllegalArgumentException
                | BadCredentialsException exception
        ) {
            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "Token JWT inválido o expirado",
                            exception
                    )
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path =
                request.getServletPath();

        return path.startsWith(
                "/swagger-ui"
        ) || path.startsWith(
                "/v3/api-docs"
        );
    }
}