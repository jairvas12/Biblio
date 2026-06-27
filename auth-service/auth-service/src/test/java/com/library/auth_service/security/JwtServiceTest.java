package com.library.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "AuthServiceJwtTestSecretKey2026Minimum32CharactersSecure";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                SECRET
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                86_400_000L
        );
    }

    @Test
    void generateToken_shouldCreateValidClaims() {

        String token =
                jwtService.generateToken(
                        15L,
                        "usuario@biblio.cl",
                        "ADMIN"
                );

        Claims claims =
                jwtService.validateAndExtractClaims(
                        token
                );

        assertNotNull(token);

        assertEquals(
                "usuario@biblio.cl",
                claims.getSubject()
        );

        assertEquals(
                "ADMIN",
                claims.get("role", String.class)
        );

        assertEquals(
                "15",
                claims.get("userId").toString()
        );

        assertNotNull(
                claims.getIssuedAt()
        );

        assertNotNull(
                claims.getExpiration()
        );

        assertTrue(
                claims.getExpiration()
                        .after(claims.getIssuedAt())
        );
    }

    @Test
    void extractUsername_shouldReturnEmail() {

        String token =
                jwtService.generateToken(
                        16L,
                        "correo@biblio.cl",
                        "USER"
                );

        String username =
                jwtService.extractUsername(token);

        assertEquals(
                "correo@biblio.cl",
                username
        );
    }

    @Test
    void extractRole_shouldReturnRole() {

        String token =
                jwtService.generateToken(
                        17L,
                        "bibliotecario@biblio.cl",
                        "BIBLIOTECARIO"
                );

        String role =
                jwtService.extractRole(token);

        assertEquals(
                "BIBLIOTECARIO",
                role
        );
    }

    @Test
    void extractUserId_shouldReturnLongValue() {

        String token =
                jwtService.generateToken(
                        18L,
                        "usuario@biblio.cl",
                        "USER"
                );

        Long userId =
                jwtService.extractUserId(token);

        assertEquals(
                18L,
                userId
        );
    }

    @Test
    void extractUserId_shouldReturnNull_whenClaimDoesNotExist() {

        Key signingKey =
                Keys.hmacShaKeyFor(
                        SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        String token =
                Jwts.builder()
                        .setSubject(
                                "sinid@biblio.cl"
                        )
                        .claim(
                                "role",
                                "USER"
                        )
                        .setIssuedAt(
                                new Date()
                        )
                        .setExpiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + 60_000
                                )
                        )
                        .signWith(
                                signingKey,
                                SignatureAlgorithm.HS256
                        )
                        .compact();

        Long userId =
                jwtService.extractUserId(token);

        assertNull(userId);
    }

    @Test
    void validateAndExtractClaims_shouldRejectExpiredToken() {

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                -1_000L
        );

        String token =
                jwtService.generateToken(
                        19L,
                        "expirado@biblio.cl",
                        "USER"
                );

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.validateAndExtractClaims(
                        token
                )
        );
    }

    @Test
    void validateAndExtractClaims_shouldRejectInvalidSignature() {

        String token =
                jwtService.generateToken(
                        20L,
                        "usuario@biblio.cl",
                        "USER"
                );

        JwtService serviceWithDifferentSecret =
                new JwtService();

        ReflectionTestUtils.setField(
                serviceWithDifferentSecret,
                "secretKey",
                "DifferentJwtSecretKey2026Minimum32CharactersSecure"
        );

        ReflectionTestUtils.setField(
                serviceWithDifferentSecret,
                "jwtExpiration",
                86_400_000L
        );

        assertThrows(
                SignatureException.class,
                () ->
                        serviceWithDifferentSecret
                                .validateAndExtractClaims(token)
        );
    }

    @Test
    void validateAndExtractClaims_shouldRejectMalformedToken() {

        assertThrows(
                JwtException.class,
                () -> jwtService.validateAndExtractClaims(
                        "token-no-valido"
                )
        );
    }
}
