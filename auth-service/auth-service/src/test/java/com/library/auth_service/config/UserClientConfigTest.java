package com.library.auth_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserClientConfigTest {

    @Test
    void internalApiKeyInterceptor_shouldAddInternalApiKeyHeader() {

        UserClientConfig config =
                new UserClientConfig();

        RequestInterceptor interceptor =
                config.internalApiKeyInterceptor(
                        "BiblioInternalTestKey2026"
                );

        RequestTemplate requestTemplate =
                new RequestTemplate();

        interceptor.apply(requestTemplate);

        Collection<String> headerValues =
                requestTemplate.headers()
                        .get("X-Internal-API-Key");

        assertNotNull(headerValues);

        assertEquals(
                1,
                headerValues.size()
        );

        assertTrue(
                headerValues.contains(
                        "BiblioInternalTestKey2026"
                )
        );
    }
}
