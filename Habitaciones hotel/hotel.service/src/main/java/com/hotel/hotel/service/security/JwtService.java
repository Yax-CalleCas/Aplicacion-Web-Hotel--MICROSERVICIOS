package com.hotel.hotel.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Valida los tokens JWT emitidos por el servicio de autenticación.
 * Este servicio NO emite tokens: solo verifica firma y expiración.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta la propiedad jwt.secret.");
        }
        String efectivo = secret.trim().length() < 48
                ? secret + "A_SECURE_COMPLEMENT_LONG_ENOUGH_FOR_HS384_AND_HS512_AUTHENTICATION_CIBERTEC_HOTEL"
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(efectivo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verifica la firma y expiración. Lanza JwtException si no es válido.
     */
    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae los roles del token y los convierte a SimpleGrantedAuthority.
     */
    public List<SimpleGrantedAuthority> authorities(Claims claims) {
        Set<String> roles = new LinkedHashSet<>();

        if (claims.get("role") instanceof String unico) {
            roles.add(unico);
        }
        if (claims.get("roles") instanceof Collection<?> varios) {
            varios.forEach(r -> roles.add(String.valueOf(r)));
        }

        return roles.stream()
                .map(String::toUpperCase)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}