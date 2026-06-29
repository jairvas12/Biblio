package com.library.find_service.config;

import com.library.find_service.controller.FindController;
import com.library.find_service.security.JwtAccessDeniedHandler;
import com.library.find_service.security.JwtAuthenticationEntryPoint;
import com.library.find_service.security.JwtAuthenticationFilter;
import com.library.find_service.security.JwtService;
import com.library.find_service.service.FindService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.http.HttpHeaders;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FindController.class
)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindService findService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getBooksShouldReturnUnauthorizedWhenTokenIsMissing()
            throws Exception {

        // Given
        // Solicitud sin token

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(401)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "UNAUTHORIZED"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                "/find/books"
                        )
                );

        verifyNoInteractions(
                findService
        );
    }

    @Test
    void getBooksShouldAllowUserRole()
            throws Exception {

        // Given
        Claims claims =
                createClaims(
                        "usuario@biblio.cl",
                        "USER"
                );

        when(
                jwtService.validateAndExtractClaims(
                        "token-user"
                )
        ).thenReturn(
                claims
        );

        when(
                findService.getAllBooks()
        ).thenReturn(
                List.of()
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer token-user"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$"
                        ).isArray()
                );
    }

    @Test
    void getBooksShouldAllowLibrarianRole()
            throws Exception {

        // Given
        Claims claims =
                createClaims(
                        "bibliotecario@biblio.cl",
                        "BIBLIOTECARIO"
                );

        when(
                jwtService.validateAndExtractClaims(
                        "token-bibliotecario"
                )
        ).thenReturn(
                claims
        );

        when(
                findService.getAllBooks()
        ).thenReturn(
                List.of()
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer token-bibliotecario"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void getBooksShouldReturnForbiddenWhenRoleIsNotAllowed()
            throws Exception {

        // Given
        Claims claims =
                createClaims(
                        "invitado@biblio.cl",
                        "INVITADO"
                );

        when(
                jwtService.validateAndExtractClaims(
                        "token-invitado"
                )
        ).thenReturn(
                claims
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer token-invitado"
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(403)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "FORBIDDEN"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "No tiene permisos para acceder a este recurso"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                "/find/books"
                        )
                );

        verifyNoInteractions(
                findService
        );
    }

    private Claims createClaims(
            String email,
            String role
    ) {
        Claims claims =
                Jwts.claims();

        claims.setSubject(
                email
        );

        claims.put(
                "role",
                role
        );

        return claims;
    }
}