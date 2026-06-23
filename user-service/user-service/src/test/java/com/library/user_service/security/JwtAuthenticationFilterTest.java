package com.library.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;

    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        jwtService =
                mock(JwtService.class);

        authenticationEntryPoint =
                mock(JwtAuthenticationEntryPoint.class);

        filter =
                new JwtAuthenticationFilter(
                        jwtService,
                        authenticationEntryPoint
                );
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinue_whenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

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
                authenticationEntryPoint,
                never()
        ).commence(
                any(),
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
    void doFilterInternal_shouldContinue_whenHeaderDoesNotStartWithBearer()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Basic credencial"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

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
        ).validateAndExtractClaims(any());
    }

    @Test
    void doFilterInternal_shouldRejectBlankBearerToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer    "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

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
        ).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid()
            throws Exception {

        String token = "token-valido";

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        Claims claims =
                mock(Claims.class);

        when(
                jwtService.validateAndExtractClaims(token)
        ).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("admin@biblio.cl");

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn("admin");

        when(claims.get("userId"))
                .thenReturn(2L);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "admin@biblio.cl",
                authentication.getPrincipal()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .contains(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
        );

        assertEquals(
                2L,
                request.getAttribute(
                        "authenticatedUserId"
                )
        );

        assertEquals(
                "ADMIN",
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
    void doFilterInternal_shouldNotReplaceExistingAuthentication()
            throws Exception {

        Authentication existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "usuario.existente@biblio.cl",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication
                );

        String token = "otro-token";

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        Claims claims =
                mock(Claims.class);

        when(
                jwtService.validateAndExtractClaims(token)
        ).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("admin@biblio.cl");

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn("ADMIN");

        when(claims.get("userId"))
                .thenReturn(2L);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication finalAuthentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertEquals(
                "usuario.existente@biblio.cl",
                finalAuthentication.getPrincipal()
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
    void doFilterInternal_shouldRejectToken_whenEmailIsMissing()
            throws Exception {

        executeInvalidClaimsTest(
                null,
                "USER",
                3L
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenEmailIsBlank()
            throws Exception {

        executeInvalidClaimsTest(
                "   ",
                "USER",
                3L
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenRoleIsMissing()
            throws Exception {

        executeInvalidClaimsTest(
                "usuario@biblio.cl",
                null,
                3L
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenRoleIsBlank()
            throws Exception {

        executeInvalidClaimsTest(
                "usuario@biblio.cl",
                "   ",
                3L
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenUserIdIsMissing()
            throws Exception {

        executeInvalidClaimsTest(
                "usuario@biblio.cl",
                "USER",
                null
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenUserIdIsInvalid()
            throws Exception {

        String token = "token-user-id-invalido";

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        Claims claims =
                mock(Claims.class);

        when(
                jwtService.validateAndExtractClaims(token)
        ).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("usuario@biblio.cl");

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn("USER");

        when(claims.get("userId"))
                .thenReturn("abc");

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenJwtServiceThrowsException()
            throws Exception {

        String token = "token-invalido";

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        when(
                jwtService.validateAndExtractClaims(token)
        ).thenThrow(
                new JwtException(
                        "Token inválido"
                )
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }

    private void executeInvalidClaimsTest(
            String email,
            String role,
            Object userId
    ) throws Exception {

        String token = "token-con-claims-invalidos";

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/users"
                );

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        Claims claims =
                mock(Claims.class);

        when(
                jwtService.validateAndExtractClaims(token)
        ).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(email);

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn(role);

        when(claims.get("userId"))
                .thenReturn(userId);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }
}


