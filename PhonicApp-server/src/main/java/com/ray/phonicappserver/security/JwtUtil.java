package com.ray.phonicappserver.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "ray-secret";
    private final long EXPIRATION = 1000 * 60 * 60 * 12; // 12小时

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
}