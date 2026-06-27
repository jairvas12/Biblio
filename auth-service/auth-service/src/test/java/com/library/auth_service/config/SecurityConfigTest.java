package com.library.auth_service.config;

import com.library.auth_service.controller.AuthController;
import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.security.JwtAuthenticationEntryPoint;
import com.library.auth_service.security.JwtAuthenticationFilter;
import com.library.auth_service.security.JwtService;
import com.library.auth_service.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldBePublic() throws Exception {

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                authService,
                never()
        ).register(
                any()
        );
    }

    @Test
    void login_shouldBePublic() throws Exception {

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                authService,
                never()
        ).login(
                any()
        );
    }

    @Test
    void changeEmail_shouldReturnUnauthorized_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/auth/admin/users/10/email"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "newEmail": "nuevo@biblio.cl"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("UNAUTHORIZED")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/auth/admin/users/10/email"
                                )
                );

        verify(
                authService,
                never()
        ).changeUserEmail(
                any(),
                any()
        );
    }

    @Test
    void changeEmail_shouldReturnForbidden_whenTokenHasUserRole()
            throws Exception {

        when(
                jwtService.validateAndExtractClaims(
                        "token-user"
                )
        ).thenReturn(
                claimsFor(
                        10L,
                        "usuario@biblio.cl",
                        "USER"
                )
        );

        mockMvc.perform(
                        patch(
                                "/auth/admin/users/10/email"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-user"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "newEmail": "nuevo@biblio.cl"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                authService,
                never()
        ).changeUserEmail(
                any(),
                any()
        );
    }

    @Test
    void changeEmail_shouldAllowAdminToken()
            throws Exception {

        when(
                jwtService.validateAndExtractClaims(
                        "token-admin"
                )
        ).thenReturn(
                claimsFor(
                        10L,
                        "admin@biblio.cl",
                        "ADMIN"
                )
        );

        UserResponseDTO updatedUser =
                UserResponseDTO.builder()
                        .id(10L)
                        .name("Usuario Actualizado")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                authService.changeUserEmail(
                        eq(10L),
                        any(ChangeEmailRequestDTO.class)
                )
        ).thenReturn(updatedUser);

        mockMvc.perform(
                        patch(
                                "/auth/admin/users/10/email"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "newEmail": "nuevo@biblio.cl"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "nuevo@biblio.cl"
                                )
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("USER")
                )
                .andExpect(result ->
                        assertNull(
                                result.getRequest()
                                        .getSession(false)
                        )
                );

        verify(authService).changeUserEmail(
                eq(10L),
                any(ChangeEmailRequestDTO.class)
        );
    }

    @Test
    void passwordEncoder_shouldUseBcrypt() {

        String encodedPassword =
                passwordEncoder.encode(
                        "ClaveSegura123"
                );

        assertTrue(
                encodedPassword.startsWith("$2")
        );

        assertTrue(
                passwordEncoder.matches(
                        "ClaveSegura123",
                        encodedPassword
                )
        );
    }

    private Claims claimsFor(
            Long userId,
            String email,
            String role
    ) {

        Claims claims =
                Jwts.claims();

        claims.setSubject(email);

        claims.put(
                "userId",
                userId
        );

        claims.put(
                "role",
                role
        );

        return claims;
    }
}
