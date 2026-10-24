package com.recepcion.hotel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y autentica la petición.
 * No consulta la base de datos: los usuarios viven en el servicio de autenticación.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Atributo de la petición que el entry point usa para elegir el mensaje 401. */
    public static final String JWT_ERROR_ATTR = "jwt_error";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Sin token: se deja pasar; las reglas de SecurityConfig deciden si el endpoint lo exige.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final Claims claims = jwtService.parse(jwt);
            final String usuario = claims.getSubject();

            if (usuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        usuario, null, jwtService.authorities(claims));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute(JWT_ERROR_ATTR, "TOKEN_EXPIRED");
        } catch (JwtException | IllegalArgumentException e) {
            // Firma alterada, token mal formado, etc.
            SecurityContextHolder.clearContext();
            request.setAttribute(JWT_ERROR_ATTR, "TOKEN_INVALID");
        }

        filterChain.doFilter(request, response);
    }
}