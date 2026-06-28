package com.library.category_service.config;

import com.library.category_service.controller.CategoryController;
import com.library.category_service.dto.CategoryRequestDTO;
import com.library.category_service.dto.CategoryResponseDTO;
import com.library.category_service.security.JwtAccessDeniedHandler;
import com.library.category_service.security.JwtAuthenticationEntryPoint;
import com.library.category_service.security.JwtAuthenticationFilter;
import com.library.category_service.security.JwtService;
import com.library.category_service.service.CategoryService;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getCategories_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/categories")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error")
                                .value("UNAUTHORIZED")
                );
    }

    @Test
    void getCategories_withUserToken_shouldReturnOk()
            throws Exception {

        configureToken(
                "user-token",
                "usuario@biblio.cl",
                "USER",
                3L
        );

        when(
                categoryService.getAllCategories()
        ).thenReturn(List.of());

        mockMvc.perform(
                        get("/categories")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void postCategory_withUserToken_shouldReturnForbidden()
            throws Exception {

        configureToken(
                "user-token",
                "usuario@biblio.cl",
                "USER",
                3L
        );

        mockMvc.perform(
                        post("/categories")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Historia"
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.error")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void postCategory_withBibliotecarioToken_shouldReturnCreated()
            throws Exception {

        configureToken(
                "bibliotecario-token",
                "bibliotecario@biblio.cl",
                "BIBLIOTECARIO",
                2L
        );

        when(
                categoryService.saveCategory(
                        any(CategoryRequestDTO.class)
                )
        ).thenReturn(
                new CategoryResponseDTO(
                        1L,
                        "Historia"
                )
        );

        mockMvc.perform(
                        post("/categories")
                                .header(
                                        "Authorization",
                                        "Bearer bibliotecario-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Historia"
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.name")
                                .value("Historia")
                );
    }

    @Test
    void deleteCategory_withBibliotecarioToken_shouldReturnForbidden()
            throws Exception {

        configureToken(
                "bibliotecario-token",
                "bibliotecario@biblio.cl",
                "BIBLIOTECARIO",
                2L
        );

        mockMvc.perform(
                        delete("/categories/1")
                                .header(
                                        "Authorization",
                                        "Bearer bibliotecario-token"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.error")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void deleteCategory_withAdminToken_shouldReturnNoContent()
            throws Exception {

        configureToken(
                "admin-token",
                "admin@biblio.cl",
                "ADMIN",
                1L
        );

        mockMvc.perform(
                        delete("/categories/1")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void getCategories_withInvalidToken_shouldReturnUnauthorized()
            throws Exception {

        when(
                jwtService.validateAndExtractClaims(
                        "invalid-token"
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Token inválido"
                )
        );

        mockMvc.perform(
                        get("/categories")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error")
                                .value("UNAUTHORIZED")
                );
    }

    private void configureToken(
            String token,
            String email,
            String role,
            Long userId
    ) {

        Claims claims = mock(Claims.class);

        when(
                jwtService.validateAndExtractClaims(
                        token
                )
        ).thenReturn(claims);

        when(
                claims.getSubject()
        ).thenReturn(email);

        when(
                claims.get(
                        "role",
                        String.class
                )
        ).thenReturn(role);

        when(
                claims.get("userId")
        ).thenReturn(userId);
    }
}