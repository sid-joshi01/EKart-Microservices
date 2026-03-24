package com.ekart.user_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static java.time.Instant.now;

@Component
public class JwtUtil {


    private final SecretKey key;
    private final long jwtExpirationInMs;


    public JwtUtil(@Value("${app.jwt.secret}") String jwtSecret,@Value("${app.jwt.expiration}") long jwtExpirationInMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(Long userId, String email ,String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString()) // convert userId to string
                .claim("email", email)
                .claim("roles", List.of(role))
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationInMs))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validateToken(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token).getBody();
        return Long.parseLong(claims.getSubject());
    }
}
