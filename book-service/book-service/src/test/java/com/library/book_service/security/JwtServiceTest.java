package com.library.book_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtServiceTest {

    private static final String SECRET =
            "01234567890123456789012345678901";

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
    }

    @Test
    void shouldExtractAllClaimsFromValidToken() {

        // Given
        String token =
                createToken(true);

        // When
        String username =
                jwtService.extractUsername(token);

        String role =
                jwtService.extractRole(token);

        Long userId =
                jwtService.extractUserId(token);

        // Then
        assertEquals(
                "admin@biblioteca.cl",
                username
        );

        assertEquals(
                "ADMIN",
                role
        );

        assertEquals(
                1L,
                userId
        );
    }

    @Test
    void extractUserIdShouldReturnNullWhenClaimIsMissing() {

        // Given
        String token =
                createToken(false);

        // When
        Long userId =
                jwtService.extractUserId(token);

        // Then
        assertNull(userId);
    }

    private String createToken(
            boolean includeUserId
    ) {

        var tokenBuilder =
                Jwts.builder()
                        .setSubject(
                                "admin@biblioteca.cl"
                        )
                        .claim(
                                "role",
                                "ADMIN"
                        );

        if (includeUserId) {
            tokenBuilder.claim(
                    "userId",
                    1L
            );
        }

        return tokenBuilder
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}

