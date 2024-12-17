package com.formato15.ebsa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtils {

    private final String jwtSecret;

    public JwtUtils(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // Obtiene el "subject" del token
    }
}

