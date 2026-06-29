package com.library.inventory_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(
            @Value("${jwt.secret}") String secretKey
    ) {
        this.secretKey = secretKey;
    }

    public Claims validateAndExtractClaims(
            String token
    ) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(
            String token
    ) {
        return validateAndExtractClaims(token)
                .getSubject();
    }

    public String extractRole(
            String token
    ) {
        return validateAndExtractClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public Long extractUserId(
            String token
    ) {
        Object userIdClaim =
                validateAndExtractClaims(token)
                        .get("userId");

        if (userIdClaim == null) {
            return null;
        }

        return Long.valueOf(
                userIdClaim.toString()
        );
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}