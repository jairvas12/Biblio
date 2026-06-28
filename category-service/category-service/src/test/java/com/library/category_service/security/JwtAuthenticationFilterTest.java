package com.library.category_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        filter =
                new JwtAuthenticationFilter(
                        jwtService,
                        authenticationEntryPoint
                );

        request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/categories"
        );

        request.setServletPath(
                "/categories"
        );

        response =
                new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verify(
                jwtService,
                never()
        ).validateAndExtractClaims(
                any()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_shouldContinueWhenHeaderIsNotBearer()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Basic credentials"
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verify(
                jwtService,
                never()
        ).validateAndExtractClaims(
                any()
        );
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorizedWhenTokenIsBlank()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer    "
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );
    }

    @Test
    void doFilterInternal_shouldAuthenticateValidToken()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        configureValidClaims();

        when(
                jwtService.validateAndExtractClaims(
                        "valid-token"
                )
        ).thenReturn(claims);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        var authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertEquals(
                "usuario@biblio.cl",
                authentication.getPrincipal()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .contains(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
        );

        assertEquals(
                3L,
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

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorizedWhenTokenIsInvalid()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        when(
                jwtService.validateAndExtractClaims(
                        "invalid-token"
                )
        ).thenThrow(
                new MalformedJwtException(
                        "Token inválido"
                )
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
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
    void doFilterInternal_shouldReturnUnauthorizedWhenRequiredClaimIsMissing()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer incomplete-token"
        );

        when(
                jwtService.validateAndExtractClaims(
                        "incomplete-token"
                )
        ).thenReturn(claims);

        when(
                claims.getSubject()
        ).thenReturn(
                "usuario@biblio.cl"
        );

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn(
                "USER"
        );

        when(
                claims.get("userId")
        ).thenReturn(null);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );
    }

    @Test
    void doFilterInternal_shouldKeepExistingAuthentication()
            throws Exception {

        UsernamePasswordAuthenticationToken
                existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@biblio.cl",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication
                );

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        configureValidClaims();

        when(
                jwtService.validateAndExtractClaims(
                        "valid-token"
                )
        ).thenReturn(claims);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

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

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForSwaggerUi() {

        request.setServletPath(
                "/swagger-ui/index.html"
        );

        assertTrue(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForApiDocs() {

        request.setServletPath(
                "/v3/api-docs"
        );

        assertTrue(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldReturnFalseForCategories() {

        request.setServletPath(
                "/categories"
        );

        assertEquals(
                false,
                filter.shouldNotFilter(request)
        );
    }

    private void configureValidClaims() {

        when(
                claims.getSubject()
        ).thenReturn(
                "usuario@biblio.cl"
        );

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn(
                "user"
        );

        when(
                claims.get("userId")
        ).thenReturn(
                3L
        );
    }
}