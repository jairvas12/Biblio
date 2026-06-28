package com.library.book_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilterDocumentationPaths() {

        // Given
        MockHttpServletRequest swaggerRequest =
                new MockHttpServletRequest();

        swaggerRequest.setServletPath(
                "/swagger-ui/index.html"
        );

        MockHttpServletRequest apiDocsRequest =
                new MockHttpServletRequest();

        apiDocsRequest.setServletPath(
                "/v3/api-docs"
        );

        MockHttpServletRequest booksRequest =
                new MockHttpServletRequest();

        booksRequest.setServletPath(
                "/books"
        );

        // When - Then
        assertTrue(
                jwtAuthenticationFilter.shouldNotFilter(
                        swaggerRequest
                )
        );

        assertTrue(
                jwtAuthenticationFilter.shouldNotFilter(
                        apiDocsRequest
                )
        );

        assertFalse(
                jwtAuthenticationFilter.shouldNotFilter(
                        booksRequest
                )
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        verify(
                filterChain
        ).doFilter(
                request,
                response
        );

        verifyNoInteractions(
                jwtService,
                authenticationEntryPoint
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldRejectBlankBearerToken()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer     "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        verify(
                authenticationEntryPoint
        ).commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );

        verifyNoInteractions(
                jwtService
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldAuthenticateValidToken()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims =
                Jwts.claims();

        claims.setSubject(
                "usuario@biblioteca.cl"
        );

        claims.put(
                "role",
                "user"
        );

        claims.put(
                "userId",
                7L
        );

        when(
                jwtService.validateAndExtractClaims(
                        "valid-token"
                )
        ).thenReturn(
                claims
        );

        // When
        jwtAuthenticationFilter.doFilterInternal(
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
                "usuario@biblioteca.cl",
                authentication.getPrincipal()
        );

        assertEquals(
                "ROLE_USER",
                authentication
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        assertEquals(
                7L,
                request.getAttribute(
                        "authenticatedUserId"
                )
        );

        assertEquals(
                "USER",
                request.getAttribute(
                        "authenticatedUserRole"
                )
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );

        verifyNoInteractions(
                authenticationEntryPoint
        );
    }
    @Test
    void shouldContinueWhenAuthorizationSchemeIsNotBearer()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        verify(
                filterChain
        ).doFilter(
                request,
                response
        );

        verifyNoInteractions(
                jwtService,
                authenticationEntryPoint
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldRejectTokenWithMissingRequiredClaims()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer incomplete-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims =
                Jwts.claims();

        claims.setSubject(
                "usuario@biblioteca.cl"
        );

        claims.put(
                "role",
                "USER"
        );

        when(
                jwtService.validateAndExtractClaims(
                        "incomplete-token"
                )
        ).thenReturn(
                claims
        );

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        verify(
                authenticationEntryPoint
        ).commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldRejectInvalidJwt()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                jwtService.validateAndExtractClaims(
                        "invalid-token"
                )
        ).thenThrow(
                new io.jsonwebtoken.MalformedJwtException(
                        "Token inválido"
                )
        );

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        verify(
                authenticationEntryPoint
        ).commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldPreserveExistingAuthentication()
            throws Exception {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims =
                Jwts.claims();

        claims.setSubject(
                "nuevo@biblioteca.cl"
        );

        claims.put(
                "role",
                "ADMIN"
        );

        claims.put(
                "userId",
                10L
        );

        when(
                jwtService.validateAndExtractClaims(
                        "valid-token"
                )
        ).thenReturn(
                claims
        );

        org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken existingAuthentication =
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        "existente@biblioteca.cl",
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication
                );

        // When
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // Then
        assertSame(
                existingAuthentication,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        assertNull(
                request.getAttribute(
                        "authenticatedUserId"
                )
        );

        assertNull(
                request.getAttribute(
                        "authenticatedUserRole"
                )
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );

        verifyNoInteractions(
                authenticationEntryPoint
        );
    }
}

