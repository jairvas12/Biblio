package com.library.user_service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_KEY =
            "clave-interna-pruebas-123456";

    private ObjectMapper objectMapper;

    private InternalApiKeyFilter filter;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        filter = new InternalApiKeyFilter(
                objectMapper
        );

        ReflectionTestUtils.setField(
                filter,
                "expectedApiKey",
                INTERNAL_KEY
        );
    }

    @Test
    void doFilterInternal_shouldContinue_whenApiKeyIsValid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/users"
                );

        request.addHeader(
                InternalApiKeyFilter.INTERNAL_HEADER,
                INTERNAL_KEY
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

        assertEquals(
                Boolean.TRUE,
                request.getAttribute(
                        "internalRequest"
                )
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorized_whenApiKeyIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
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

        assertEquals(
                401,
                response.getStatus()
        );

        JsonNode responseBody =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertEquals(
                401,
                responseBody.get("status").asInt()
        );

        assertEquals(
                "INVALID INTERNAL CREDENTIAL",
                responseBody.get("error").asText()
        );

        assertEquals(
                "/users",
                responseBody.get("path").asText()
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorized_whenApiKeyIsBlank()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/users"
                );

        request.addHeader(
                InternalApiKeyFilter.INTERNAL_HEADER,
                "   "
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

        assertEquals(
                401,
                response.getStatus()
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorized_whenApiKeyIsIncorrect()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/users"
                );

        request.addHeader(
                InternalApiKeyFilter.INTERNAL_HEADER,
                "clave-incorrecta"
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

        assertEquals(
                401,
                response.getStatus()
        );

        verify(
                filterChain,
                never()
        ).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_shouldProtectCreateUser() {

        MockHttpServletRequest request =
                createRequest(
                        "POST",
                        "/users"
                );

        assertFalse(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldProtectFindByEmail() {

        MockHttpServletRequest request =
                createRequest(
                        "GET",
                        "/users/email/usuario@biblio.cl"
                );

        assertFalse(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldProtectInternalDeactivation() {

        MockHttpServletRequest request =
                createRequest(
                        "DELETE",
                        "/users/internal/3/deactivate"
                );

        assertFalse(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldProtectInternalEmailUpdate() {

        MockHttpServletRequest request =
                createRequest(
                        "PUT",
                        "/users/internal/3/email"
                );

        assertFalse(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldSkipNormalUserEndpoint() {

        MockHttpServletRequest request =
                createRequest(
                        "GET",
                        "/users/3"
                );

        assertTrue(
                filter.shouldNotFilter(request)
        );
    }

    @Test
    void shouldNotFilter_shouldSkipInvalidInternalPath() {

        MockHttpServletRequest request =
                createRequest(
                        "PUT",
                        "/users/internal/abc/email"
                );

        assertTrue(
                filter.shouldNotFilter(request)
        );
    }

    private MockHttpServletRequest createRequest(
            String method,
            String servletPath
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod(method);
        request.setServletPath(servletPath);

        return request;
    }
}


