package com.library.notification_service.security;

import com.library.notification_service.service.NotificationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationSecurityTest {

    private static final String SECRET =
            "MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private NotificationService notificationService;

    @Test
    void getSinTokenDebeRetornar401() throws Exception {

        mockMvc.perform(
                        get("/notifications/1")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getConRolUserDebeRetornar200() throws Exception {

        mockMvc.perform(
                        get("/notifications/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken("USER")
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void postConRolUserDebeRetornar403() throws Exception {

        mockMvc.perform(
                        post("/notifications")
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
                        post("/notifications")
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
    void patchConRolUserDebeRetornar200()
            throws Exception {

        mockMvc.perform(
                        patch("/notifications/1/read")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken("USER")
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteConRolBibliotecarioDebeRetornar403()
            throws Exception {

        mockMvc.perform(
                        delete("/notifications/1")
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
                .when(notificationService)
                .delete(1L);

        mockMvc.perform(
                        delete("/notifications/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + crearToken("ADMIN")
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void tokenInvalidoDebeRetornar401()
            throws Exception {

        mockMvc.perform(
                        get("/notifications/1")
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
                        get("/notifications/1")
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
                        get("/notifications/1")
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
                "USER",
                10L
        );

        mockMvc.perform(
                        get("/notifications/1")
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
                "USER",
                10L
        );

        mockMvc.perform(
                        get("/notifications/1")
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
                        get("/notifications/1")
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
                        get("/notifications/1")
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
                "USER",
                null
        );

        mockMvc.perform(
                        get("/notifications/1")
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
                "USER",
                "abc"
        );

        mockMvc.perform(
                        get("/notifications/1")
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
                "ROLE_USER",
                10L
        );

        mockMvc.perform(
                        get("/notifications/1")
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
                "user",
                10L
        );

        mockMvc.perform(
                        get("/notifications/1")
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
    void notificationsDebeUsarFiltroJwt() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath(
                "/notifications/1"
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

        Date expiration = new Date(
                now.getTime() + 3_600_000
        );

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiration);

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
                  "title": "Préstamo próximo a vencer",
                  "message": "Debe devolver el libro mañana",
                  "type": "PRESTAMO"
                }
                """;
    }
}