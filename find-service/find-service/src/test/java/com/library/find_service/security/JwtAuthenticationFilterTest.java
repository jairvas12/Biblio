package com.library.find_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtService
                );
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void filterShouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/find/books"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        // When
        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        // Then
        assertNotNull(
                filterChain.getRequest()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(
                jwtService
        );
    }

    @Test
    void filterShouldContinueWhenBearerTokenIsBlank()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/find/books"
        );

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer    "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        // When
        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        // Then
        assertNotNull(
                filterChain.getRequest()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(
                jwtService
        );
    }

    @Test
    void filterShouldContinueWithoutAuthenticationWhenTokenIsInvalid()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/find/books"
        );

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-invalido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        when(
                jwtService.validateAndExtractClaims(
                        "token-invalido"
                )
        ).thenThrow(
                new MalformedJwtException(
                        "Token inválido"
                )
        );

        // When
        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        // Then
        assertNotNull(
                filterChain.getRequest()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(
                jwtService
        ).validateAndExtractClaims(
                "token-invalido"
        );
    }

    @Test
    void filterShouldAuthenticateWhenRoleAlreadyContainsPrefix()
            throws Exception {

        // Given
        Claims claims =
                Jwts.claims();

        claims.setSubject(
                "admin@biblio.cl"
        );

        claims.put(
                "role",
                "ROLE_ADMIN"
        );

        when(
                jwtService.validateAndExtractClaims(
                        "token-admin"
                )
        ).thenReturn(
                claims
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/find/books"
        );

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-admin"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        // When
        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        // Then
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(
                authentication
        );

        assertEquals(
                "admin@biblio.cl",
                authentication.getPrincipal()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_ADMIN"
                                        )
                        )
        );

        assertNotNull(
                filterChain.getRequest()
        );

        verify(
                jwtService
        ).validateAndExtractClaims(
                "token-admin"
        );
    }
}