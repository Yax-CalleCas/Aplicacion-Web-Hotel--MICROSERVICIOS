package com.recepcion.hotel.security;

import io.jsonwebtoken.Claims;
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
 * El secreto (jwt.secret) debe ser EXACTAMENTE el mismo que usa el servicio de auth.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta la propiedad jwt.secret (variable de entorno JWT_SECRET).");
        }
        // Misma regla que el servicio de auth, para que ambas claves coincidan.
        // Recomendado: usar un secreto de 48 caracteres o más en los dos servicios.
        String efectivo = secret.trim().length() < 48
                ? secret + "A_SECURE_COMPLEMENT_LONG_ENOUGH_FOR_HS384_AND_HS512_AUTHENTICATION_CIBERTEC_HOTEL"
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(efectivo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verifica la firma y la expiración. Lanza JwtException si el token no es válido
     * (firma alterada, mal formado o vencido).
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convierte los claims "role" (texto) o "roles" (lista) en autoridades de Spring.
     * Si el token no trae roles, devuelve una lista vacía (el usuario queda autenticado, sin rol).
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