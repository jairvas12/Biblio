package com.library.category_service.security;

import io.jsonwebtoken.Claims;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    private static final String SECRET =
            "MyVeryLongSuperSecretJwtKeyForLibraryManagementSystem2026SecureTokenGeneration";

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

        String token =
                createToken(true);

        Claims claims =
                jwtService.validateAndExtractClaims(
                        token
                );

        assertNotNull(claims);

        assertEquals(
                "usuario@biblio.cl",
                claims.getSubject()
        );

        assertEquals(
                "USER",
                claims.get(
                        "role",
                        String.class
                )
        );
    }

    @Test
    void extractUsername_shouldReturnTokenSubject() {

        String token =
                createToken(true);

        String username =
                jwtService.extractUsername(token);

        assertEquals(
                "usuario@biblio.cl",
                username
        );
    }

    @Test
    void extractRole_shouldReturnTokenRole() {

        String token =
                createToken(true);

        String role =
                jwtService.extractRole(token);

        assertEquals(
                "USER",
                role
        );
    }

    @Test
    void extractUserId_shouldReturnUserIdWhenClaimExists() {

        String token =
                createToken(true);

        Long userId =
                jwtService.extractUserId(token);

        assertEquals(
                3L,
                userId
        );
    }

    @Test
    void extractUserId_shouldReturnNullWhenClaimDoesNotExist() {

        String token =
                createToken(false);

        Long userId =
                jwtService.extractUserId(token);

        assertNull(userId);
    }

    private String createToken(
            boolean includeUserId
    ) {

        Key signingKey =
                Keys.hmacShaKeyFor(
                        SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        var tokenBuilder =
                Jwts.builder()
                        .setSubject(
                                "usuario@biblio.cl"
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
                                                + 60000
                                )
                        );

        if (includeUserId) {
            tokenBuilder.claim(
                    "userId",
                    3L
            );
        }

        return tokenBuilder
                .signWith(
                        signingKey,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}