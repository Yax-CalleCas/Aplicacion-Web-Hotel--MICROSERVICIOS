package com.auth.seguridad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        this.expirationTime = expiration;

        String secureSecret = (secret == null || secret.trim().length() < 48)
                ? (secret + "A_SECURE_COMPLEMENT_LONG_ENOUGH_FOR_HS384_AND_HS512_AUTHENTICATION_CIBERTEC_HOTEL")
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(secureSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera el token JWT incluyendo el correo y el rol en los claims.
     */
    public String generateToken(String correo, String rol) {
        return Jwts.builder()
                .subject(correo)
                .claim("role", rol) // <-- Se incluye el rol para los demás microservicios
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String correo) {
        final String username = extractUsername(token);
        return (username.equals(correo) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}