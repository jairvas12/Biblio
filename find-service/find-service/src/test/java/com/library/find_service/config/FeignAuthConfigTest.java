package com.library.find_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignAuthConfigTest {

    private RequestInterceptor interceptor;

    @BeforeEach
    void setUp() {

        interceptor =
                new FeignAuthConfig()
                        .authorizationHeaderInterceptor();

        RequestContextHolder
                .resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {

        RequestContextHolder
                .resetRequestAttributes();
    }

    @Test
    void interceptorShouldDoNothingWhenRequestContextIsMissing() {

        // Given
        RequestTemplate requestTemplate =
                new RequestTemplate();

        // When
        interceptor.apply(
                requestTemplate
        );

        // Then
        assertFalse(
                requestTemplate
                        .headers()
                        .containsKey(
                                HttpHeaders.AUTHORIZATION
                        )
        );
    }

    @Test
    void interceptorShouldNotForwardHeaderWhenAuthorizationIsMissing() {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        RequestContextHolder
                .setRequestAttributes(
                        new ServletRequestAttributes(
                                request
                        )
                );

        RequestTemplate requestTemplate =
                new RequestTemplate();

        // When
        interceptor.apply(
                requestTemplate
        );

        // Then
        assertFalse(
                requestTemplate
                        .headers()
                        .containsKey(
                                HttpHeaders.AUTHORIZATION
                        )
        );
    }

    @Test
    void interceptorShouldNotForwardHeaderWhenAuthorizationIsBlank() {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "   "
        );

        RequestContextHolder
                .setRequestAttributes(
                        new ServletRequestAttributes(
                                request
                        )
                );

        RequestTemplate requestTemplate =
                new RequestTemplate();

        // When
        interceptor.apply(
                requestTemplate
        );

        // Then
        assertFalse(
                requestTemplate
                        .headers()
                        .containsKey(
                                HttpHeaders.AUTHORIZATION
                        )
        );
    }

    @Test
    void interceptorShouldForwardValidAuthorizationHeader() {

        // Given
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-user"
        );

        RequestContextHolder
                .setRequestAttributes(
                        new ServletRequestAttributes(
                                request
                        )
                );

        RequestTemplate requestTemplate =
                new RequestTemplate();

        // When
        interceptor.apply(
                requestTemplate
        );

        // Then
        Collection<String> authorizationHeaders =
                requestTemplate
                        .headers()
                        .get(
                                HttpHeaders.AUTHORIZATION
                        );

        assertNotNull(
                authorizationHeaders
        );

        assertTrue(
                authorizationHeaders.contains(
                        "Bearer token-user"
                )
        );
    }
}