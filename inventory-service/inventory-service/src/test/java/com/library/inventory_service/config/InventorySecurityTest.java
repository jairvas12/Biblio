package com.library.inventory_service.config;

import com.library.inventory_service.controller.InventoryController;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import com.library.inventory_service.dto.InventoryMovementRequestDTO;
import com.library.inventory_service.security.JwtAccessDeniedHandler;
import com.library.inventory_service.security.JwtAuthenticationEntryPoint;
import com.library.inventory_service.security.JwtAuthenticationFilter;
import com.library.inventory_service.security.JwtService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import com.library.inventory_service.service.InventoryService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@Import({
        SecurityConfig.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
@TestPropertySource(properties = {
        "jwt.secret=MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration"
})
class InventorySecurityTest {

    private static final String SECRET =
            "MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void listarMovimientosSinTokenDevuelve401()
            throws Exception {

        mockMvc.perform(
                        get("/inventory/movements")
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("UNAUTHORIZED")
                );

        verify(
                inventoryService,
                never()
        ).listarMovimientos();
    }

    @Test
    void listarMovimientosConRolUserDevuelve200()
            throws Exception {

        when(
                inventoryService.listarMovimientos()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/inventory/movements")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken(
                                                "USER"
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                inventoryService
        ).listarMovimientos();
    }

    @Test
    void registrarMovimientoConRolUserDevuelve403()
            throws Exception {

        mockMvc.perform(
                        post("/inventory/movements")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken(
                                                "USER"
                                        )
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        movimientoJson()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.status").value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("FORBIDDEN")
                );

        verify(
                inventoryService,
                never()
        ).registrarMovimiento(
                any(InventoryMovementRequestDTO.class)
        );
    }

    @Test
    void registrarMovimientoConBibliotecarioDevuelve201()
            throws Exception {

        when(
                inventoryService.registrarMovimiento(
                        any(InventoryMovementRequestDTO.class)
                )
        ).thenReturn(null);

        mockMvc.perform(
                        post("/inventory/movements")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken(
                                                "BIBLIOTECARIO"
                                        )
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        movimientoJson()
                                )
                )
                .andExpect(
                        status().isCreated()
                );

        verify(
                inventoryService
        ).registrarMovimiento(
                any(InventoryMovementRequestDTO.class)
        );
    }

    @Test
void eliminarMovimientoConRolUserDevuelve403()
        throws Exception {

    mockMvc.perform(
                    delete("/inventory/movements/10")
                            .header(
                                    "Authorization",
                                    "Bearer " + crearToken(
                                            "USER"
                                    )
                            )
            )
            .andExpect(
                    status().isForbidden()
            )
            .andExpect(
                    jsonPath("$.status").value(403)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("FORBIDDEN")
            );

    verify(
            inventoryService,
            never()
    ).eliminarMovimiento(10L);
}

@Test
void eliminarMovimientoConRolAdminDevuelve204()
        throws Exception {

    mockMvc.perform(
                    delete("/inventory/movements/10")
                            .header(
                                    "Authorization",
                                    "Bearer " + crearToken(
                                            "ADMIN"
                                    )
                            )
            )
            .andExpect(
                    status().isNoContent()
            );

    verify(
            inventoryService
    ).eliminarMovimiento(10L);
}

@Test
void listarMovimientosConTokenInvalidoDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Bearer token-invalido"
                            )
            )
            .andExpect(
                    status().isUnauthorized()
            )
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}

@Test
void listarMovimientosConTokenVacioDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Bearer    "
                            )
            )
            .andExpect(
                    status().isUnauthorized()
            )
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}
    @Test
void listarMovimientosConTokenSinSubjectDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Bearer " + crearTokenSinSubject()
                            )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}

@Test
void listarMovimientosConTokenSinUserIdDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Bearer " + crearTokenSinUserId()
                            )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}
    
    @Test
void listarMovimientosConRolVacioDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Bearer " + crearTokenConRolVacio()
                            )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}

@Test
void listarMovimientosConEsquemaIncorrectoDevuelve401()
        throws Exception {

    mockMvc.perform(
                    get("/inventory/movements")
                            .header(
                                    "Authorization",
                                    "Basic credenciales-invalidas"
                            )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                    jsonPath("$.status").value(401)
            )
            .andExpect(
                    jsonPath("$.error")
                            .value("UNAUTHORIZED")
            );

    verify(
            inventoryService,
            never()
    ).listarMovimientos();
}

    private String crearToken(
            String role
    ) {
        return Jwts.builder()
                .setSubject(
                        "usuario@biblioteca.cl"
                )
                .claim(
                        "role",
                        role
                )
                .claim(
                        "userId",
                        1L
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
    private String crearTokenSinSubject() {
    return Jwts.builder()
            .claim("role", "USER")
            .claim("userId", 1L)
            .signWith(
                    Keys.hmacShaKeyFor(
                            SECRET.getBytes(StandardCharsets.UTF_8)
                    ),
                    SignatureAlgorithm.HS256
            )
            .compact();
}

private String crearTokenConRolVacio() {
    return Jwts.builder()
            .setSubject("usuario@biblioteca.cl")
            .claim("role", "   ")
            .claim("userId", 1L)
            .signWith(
                    Keys.hmacShaKeyFor(
                            SECRET.getBytes(StandardCharsets.UTF_8)
                    ),
                    SignatureAlgorithm.HS256
            )
            .compact();
}

private String crearTokenSinUserId() {
    return Jwts.builder()
            .setSubject("usuario@biblioteca.cl")
            .claim("role", "USER")
            .signWith(
                    Keys.hmacShaKeyFor(
                            SECRET.getBytes(StandardCharsets.UTF_8)
                    ),
                    SignatureAlgorithm.HS256
            )
            .compact();
}
    private String movimientoJson() {
        return """
                {
                  "bookId": 1,
                  "tipoMovimiento": "ENTRADA",
                  "cantidad": 5,
                  "motivo": "Ingreso de ejemplares",
                  "responsable": "Bastian"
                }
                """;
    }
}