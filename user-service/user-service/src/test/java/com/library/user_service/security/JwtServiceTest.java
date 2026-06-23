package com.library.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET =
            "01234567890123456789012345678901";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                SECRET
        );
    }

    @Test
    void validateAndExtractClaims_shouldReturnClaims() {

        String token = buildToken(
                "admin@biblio.cl",
                "ADMIN",
                2L,
                SECRET
        );

        Claims claims =
                jwtService.validateAndExtractClaims(token);

        assertEquals(
                "admin@biblio.cl",
                claims.getSubject()
        );

        assertEquals(
                "ADMIN",
                claims.get("role", String.class)
        );

        assertEquals(
                "2",
                claims.get("userId").toString()
        );
    }

    @Test
    void extractEmail_shouldReturnEmail() {

        String token = buildToken(
                "usuario@biblio.cl",
                "USER",
                3L,
                SECRET
        );

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "usuario@biblio.cl",
                email
        );
    }

    @Test
    void extractRole_shouldReturnRole() {

        String token = buildToken(
                "bibliotecario@biblio.cl",
                "BIBLIOTECARIO",
                4L,
                SECRET
        );

        String role =
                jwtService.extractRole(token);

        assertEquals(
                "BIBLIOTECARIO",
                role
        );
    }

    @Test
    void extractUserId_shouldReturnUserId() {

        String token = buildToken(
                "usuario@biblio.cl",
                "USER",
                25L,
                SECRET
        );

        Long userId =
                jwtService.extractUserId(token);

        assertEquals(25L, userId);
    }

    @Test
    void extractUserId_shouldReturnNull_whenUserIdIsMissing() {

        String token = buildToken(
                "usuario@biblio.cl",
                "USER",
                null,
                SECRET
        );

        Long userId =
                jwtService.extractUserId(token);

        assertNull(userId);
    }

    @Test
    void validateAndExtractClaims_shouldThrowException_whenSignatureIsInvalid() {

        String differentSecret =
                "abcdefghijklmnopqrstuvwxyz123456";

        String token = buildToken(
                "usuario@biblio.cl",
                "USER",
                3L,
                differentSecret
        );

        assertThrows(
                JwtException.class,
                () -> jwtService
                        .validateAndExtractClaims(token)
        );
    }

    private String buildToken(
            String email,
            String role,
            Long userId,
            String secret
    ) {

        Key key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        var tokenBuilder =
                Jwts.builder()
                        .setSubject(email)
                        .claim("role", role)
                        .setIssuedAt(new Date())
                        .setExpiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + 60_000
                                )
                        );

        if (userId != null) {
            tokenBuilder.claim(
                    "userId",
                    userId
            );
        }

        return tokenBuilder
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}


