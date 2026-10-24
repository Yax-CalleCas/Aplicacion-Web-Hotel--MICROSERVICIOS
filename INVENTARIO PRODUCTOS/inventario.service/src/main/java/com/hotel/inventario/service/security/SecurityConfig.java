package com.hotel.inventario.service.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS lo gestiona el gateway / proxy de Angular, no este servicio
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) -> {
                            Object causa = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTR);
                            String mensaje = "TOKEN_EXPIRED".equals(causa)
                                    ? "La sesión expiró. Inicia sesión nuevamente."
                                    : "TOKEN_INVALID".equals(causa)
                                    ? "Token inválido."
                                    : "Debes iniciar sesión para acceder a este recurso.";
                            escribirJson(response, HttpServletResponse.SC_UNAUTHORIZED, mensaje);
                        })
                        .accessDeniedHandler((request, response, ex) ->
                                escribirJson(response, HttpServletResponse.SC_FORBIDDEN,
                                        "No tienes permiso para realizar esta acción.")))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/habitacion/**",
                                "/api/carrito/**",
                                "/api/producto/**",
                                "/api/venta/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void escribirJson(HttpServletResponse response, int status, String mensaje)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String escapado = mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"success\":false,\"status\":" + status + ",\"message\":\"" + escapado + "\"}");
    }
}