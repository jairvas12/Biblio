package com.library.security_service.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtServiceTest {

    private static final String SECRET =
            "MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(SECRET);
    }

    @Test
    void extractUsernameDebeRetornarSubject() {

        String token = crearToken(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                10L
        );

        String result =
                jwtService.extractUsername(token);

        assertEquals(
                "usuario@biblio.cl",
                result
        );
    }

    @Test
    void extractRoleDebeRetornarRol() {

        String token = crearToken(
                "usuario@biblio.cl",
                "ADMIN",
                10L
        );

        String result =
                jwtService.extractRole(token);

        assertEquals("ADMIN", result);
    }

    @Test
    void extractUserIdDebeRetornarIdNumerico() {

        String token = crearToken(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                10L
        );

        Long result =
                jwtService.extractUserId(token);

        assertEquals(10L, result);
    }

    @Test
    void extractUserIdDebeAceptarClaimComoTexto() {

        String token = crearToken(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                "25"
        );

        Long result =
                jwtService.extractUserId(token);

        assertEquals(25L, result);
    }

    @Test
    void extractUserIdDebeRetornarNullCuandoNoExiste() {

        String token = crearToken(
                "usuario@biblio.cl",
                "BIBLIOTECARIO",
                null
        );

        Long result =
                jwtService.extractUserId(token);

        assertNull(result);
    }

    private String crearToken(
            String subject,
            String role,
            Object userId
    ) {

        Key key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(
                        new Date(
                                now.getTime() + 3_600_000
                        )
                );

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
}