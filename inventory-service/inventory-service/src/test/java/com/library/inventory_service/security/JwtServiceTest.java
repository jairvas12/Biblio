package com.library.inventory_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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
    void extractUsernameDevuelveCorreo() {
        String token = crearTokenCompleto();

        String username =
                jwtService.extractUsername(token);

        assertEquals(
                "usuario@biblioteca.cl",
                username
        );
    }

    @Test
    void extractRoleDevuelveRol() {
        String token = crearTokenCompleto();

        String role =
                jwtService.extractRole(token);

        assertEquals(
                "USER",
                role
        );
    }

    @Test
    void extractUserIdDevuelveId() {
        String token = crearTokenCompleto();

        Long userId =
                jwtService.extractUserId(token);

        assertEquals(
                1L,
                userId
        );
    }

    @Test
    void extractUserIdSinClaimDevuelveNull() {
        String token =
                Jwts.builder()
                        .setSubject(
                                "usuario@biblioteca.cl"
                        )
                        .claim(
                                "role",
                                "USER"
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

        Long userId =
                jwtService.extractUserId(token);

        assertNull(userId);
    }

    private String crearTokenCompleto() {
        return Jwts.builder()
                .setSubject(
                        "usuario@biblioteca.cl"
                )
                .claim(
                        "role",
                        "USER"
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
}