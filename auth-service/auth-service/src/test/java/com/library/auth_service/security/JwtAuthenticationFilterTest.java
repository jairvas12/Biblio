package com.library.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
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
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinue_whenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
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
    void doFilterInternal_shouldContinue_whenHeaderIsNotBearer()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic credenciales"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verifyNoInteractions(
                jwtService,
                authenticationEntryPoint
        );
    }

    @Test
    void doFilterInternal_shouldRejectBlankBearerToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer     "
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint).commence(
                same(request),
                same(response),
                any(BadCredentialsException.class)
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                any(),
                any()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldAuthenticateValidToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-valido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims =
                validClaims();

        when(
                jwtService.validateAndExtractClaims(
                        "token-valido"
                )
        ).thenReturn(claims);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
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
                                        "ROLE_ADMIN"
                                )
                        )
        );

        assertEquals(
                25L,
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

        verifyNoInteractions(
                authenticationEntryPoint
        );
    }

    @Test
    void doFilterInternal_shouldPreserveExistingAuthentication()
            throws Exception {

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "existente@biblio.cl",
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

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-valido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                jwtService.validateAndExtractClaims(
                        "token-valido"
                )
        ).thenReturn(
                validClaims()
        );

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

        assertNull(
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
    void doFilterInternal_shouldRejectToken_whenEmailIsNull()
            throws Exception {

        Claims claims =
                validClaims();

        claims.setSubject(null);

        executeRejectedClaimsTest(
                claims,
                "token-sin-email"
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenEmailIsBlank()
            throws Exception {

        Claims claims =
                validClaims();

        claims.setSubject("   ");

        executeRejectedClaimsTest(
                claims,
                "token-email-vacio"
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenRoleIsNull()
            throws Exception {

        Claims claims =
                validClaims();

        claims.remove("role");

        executeRejectedClaimsTest(
                claims,
                "token-sin-role"
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenRoleIsBlank()
            throws Exception {

        Claims claims =
                validClaims();

        claims.put(
                "role",
                "   "
        );

        executeRejectedClaimsTest(
                claims,
                "token-role-vacio"
        );
    }

    @Test
    void doFilterInternal_shouldRejectToken_whenUserIdIsMissing()
            throws Exception {

        Claims claims =
                validClaims();

        claims.remove("userId");

        executeRejectedClaimsTest(
                claims,
                "token-sin-userid"
        );
    }

    @Test
    void doFilterInternal_shouldRejectInvalidJwt()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                jwtService.validateAndExtractClaims(
                        "token-invalido"
                )
        ).thenThrow(
                new MalformedJwtException(
                        "Token malformado"
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
                same(request),
                same(response),
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
    void doFilterInternal_shouldRejectNonNumericUserId()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-userid-invalido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims =
                validClaims();

        claims.put(
                "userId",
                "valor-no-numerico"
        );

        when(
                jwtService.validateAndExtractClaims(
                        "token-userid-invalido"
                )
        ).thenReturn(claims);

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
                same(request),
                same(response),
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
    void shouldNotFilter_shouldExcludePublicAndSwaggerRoutes() {

        assertTrue(
                filter.shouldNotFilter(
                        requestWithPath(
                                "/auth/login"
                        )
                )
        );

        assertTrue(
                filter.shouldNotFilter(
                        requestWithPath(
                                "/auth/register"
                        )
                )
        );

        assertTrue(
                filter.shouldNotFilter(
                        requestWithPath(
                                "/swagger-ui/index.html"
                        )
                )
        );

        assertTrue(
                filter.shouldNotFilter(
                        requestWithPath(
                                "/v3/api-docs"
                        )
                )
        );

        assertFalse(
                filter.shouldNotFilter(
                        requestWithPath(
                                "/auth/admin/users/10/email"
                        )
                )
        );
    }

    private void executeRejectedClaimsTest(
            Claims claims,
            String token
    ) throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                jwtService.validateAndExtractClaims(
                        token
                )
        ).thenReturn(claims);

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
                same(request),
                same(response),
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

    private Claims validClaims() {

        Claims claims =
                Jwts.claims();

        claims.setSubject(
                "usuario@biblio.cl"
        );

        claims.put(
                "role",
                " admin "
        );

        claims.put(
                "userId",
                25L
        );

        return claims;
    }

    private MockHttpServletRequest requestWithPath(
            String path
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath(path);

        return request;
    }
}
