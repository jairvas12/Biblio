
package com.library.copia_service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.copia_service.config.FeignAuthConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecuritySupportTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void authenticationEntryPointDevuelve401() throws Exception {
        JwtAuthenticationEntryPoint entryPoint =
                new JwtAuthenticationEntryPoint(
                        objectMapper
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/copias");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                mock(AuthenticationException.class);

        when(exception.getMessage())
                .thenReturn("Token JWT requerido");

        entryPoint.commence(
                request,
                response,
                exception
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertTrue(
                response.getContentType()
                        .startsWith("application/json")
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );

        Map<String, Object> body =
                objectMapper.readValue(
                        response.getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assertNotNull(body.get("timestamp"));

        assertEquals(
                401,
                ((Number) body.get("status")).intValue()
        );

        assertEquals(
                "UNAUTHORIZED",
                body.get("error")
        );

        assertEquals(
                "Token JWT requerido",
                body.get("message")
        );

        assertEquals(
                "/copias",
                body.get("path")
        );
    }

    @Test
    void accessDeniedHandlerDevuelve403() throws Exception {
        JwtAccessDeniedHandler accessDeniedHandler =
                new JwtAccessDeniedHandler(
                        objectMapper
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/copias/10");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AccessDeniedException exception =
                new AccessDeniedException(
                        "Acceso denegado"
                );

        accessDeniedHandler.handle(
                request,
                response,
                exception
        );

        assertEquals(
                403,
                response.getStatus()
        );

        assertTrue(
                response.getContentType()
                        .startsWith("application/json")
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );

        Map<String, Object> body =
                objectMapper.readValue(
                        response.getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assertNotNull(body.get("timestamp"));

        assertEquals(
                403,
                ((Number) body.get("status")).intValue()
        );

        assertEquals(
                "FORBIDDEN",
                body.get("error")
        );

        assertEquals(
                "No tiene permisos para acceder a este recurso",
                body.get("message")
        );

        assertEquals(
                "/copias/10",
                body.get("path")
        );
    }

    @Test
    void feignAuthConfigReenviaAuthorization() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-prueba"
        );

        ServletRequestAttributes attributes =
                new ServletRequestAttributes(request);

        RequestContextHolder.setRequestAttributes(
                attributes
        );

        try {
            FeignAuthConfig config =
                    new FeignAuthConfig();

            RequestInterceptor interceptor =
                    config.authorizationRequestInterceptor();

            RequestTemplate requestTemplate =
                    new RequestTemplate();

            interceptor.apply(requestTemplate);

            assertNotNull(
                    requestTemplate.headers()
                            .get(HttpHeaders.AUTHORIZATION)
            );

            assertTrue(
                    requestTemplate.headers()
                            .get(HttpHeaders.AUTHORIZATION)
                            .contains("Bearer token-prueba")
            );
        } finally {
            RequestContextHolder.resetRequestAttributes();
            attributes.requestCompleted();
        }
    }
}
