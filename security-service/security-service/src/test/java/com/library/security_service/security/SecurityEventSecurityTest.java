package com.library.security_service.security;

import com.library.security_service.service.SecurityEventService;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityEventSecurityTest {

    private static final String SECRET =
            "MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private SecurityEventService securityEventService;

    @Test
    void getSinTokenDebeRetornar401() throws Exception {

        mockMvc.perform(
                        get("/security/events")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getConRolUserDebeRetornar403() throws Exception {

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken("USER")
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getConRolBibliotecarioDebeRetornar200()
            throws Exception {

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + crearToken(
                                                        "BIBLIOTECARIO"
                                                )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void postConRolUserDebeRetornar403()
            throws Exception {

        mockMvc.perform(
                        post("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken("USER")
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonValido())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void postConRolBibliotecarioDebeRetornar201()
            throws Exception {

        mockMvc.perform(
                        post("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + crearToken(
                                                        "BIBLIOTECARIO"
                                                )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonValido())
                )
                .andExpect(status().isCreated());
    }

    @Test
    void deleteConRolBibliotecarioDebeRetornar403()
            throws Exception {

        mockMvc.perform(
                        delete("/security/events/1")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + crearToken(
                                                        "BIBLIOTECARIO"
                                                )
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteConRolAdminDebeRetornar204()
            throws Exception {

        doNothing()
                .when(securityEventService)
                .delete(1L);

        mockMvc.perform(
                        delete("/security/events/1")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + crearToken("ADMIN")
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void tokenInvalidoDebeRetornar401()
            throws Exception {

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer token-invalido"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenVacioDebeRetornar401()
            throws Exception {

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void esquemaBasicDebeRetornar401()
            throws Exception {

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Basic credenciales"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinSubjectDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                null,
                "BIBLIOTECARIO",
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenConSubjectVacioDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                " ",
                "BIBLIOTECARIO",
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinRolDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                null,
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenConRolVacioDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                " ",
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinUserIdDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                null
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenConUserIdInvalidoDebeRetornar401()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                "abc"
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rolConPrefijoRoleDebeRetornar200()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                "ROLE_BIBLIOTECARIO",
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void rolEnMinusculasDebeRetornar200()
            throws Exception {

        String token = crearTokenConDatos(
                "usuario@biblio.cl",
                "bibliotecario",
                10L
        );

        mockMvc.perform(
                        get("/security/events")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUiNoDebeUsarFiltroJwt() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath(
                "/swagger-ui/index.html"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    @Test
    void apiDocsNoDebeUsarFiltroJwt() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath(
                "/v3/api-docs"
        );

        assertTrue(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    @Test
    void rutaSecurityDebeUsarFiltroJwt() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath(
                "/security/events"
        );

        assertFalse(
                jwtAuthenticationFilter
                        .shouldNotFilter(request)
        );
    }

    private String crearToken(String role) {

        return crearTokenConDatos(
                "usuario@biblio.cl",
                role,
                10L
        );
    }

    private String crearTokenConDatos(
            String subject,
            String role,
            Object userId
    ) {

        Key key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(
                        new Date(
                                now.getTime() + 3_600_000
                        )
                );

        if (subject != null) {
            builder.setSubject(subject);
        }

        if (role != null) {
            builder.claim("role", role);
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    private String jsonValido() {

        return """
                {
                  "userId": 10,
                  "username": "usuario@biblio.cl",
                  "type": "LOGIN_SUCCESS",
                  "description": "Inicio de sesión correcto",
                  "ipAddress": "127.0.0.1",
                  "successful": true
                }
                """;
    }
}