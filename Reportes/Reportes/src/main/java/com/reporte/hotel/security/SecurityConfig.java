package com.reporte.hotel.security;

import jakarta.servlet.DispatcherType;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Sin token o token inválido -> 401. Token válido sin permiso -> 403.
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
                        // Preflight de CORS (Angular lo envía antes de cada petición)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Despachos internos de Tomcat (ASYNC / ERROR). Necesario para SSE:
                        // cuando el cliente cierra el stream, Tomcat re-despacha la petición ya
                        // Las peticiones reales (DispatcherType.REQUEST) siguen exigiendo JWT.
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        // Rutas públicas de salud o documentación
                        .requestMatchers("/actuator/health", "/actuator/health/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // 💡 Todos los endpoints de reportes exigen token JWT válido
                        .requestMatchers("/api/reportes/**").authenticated()

                        // Estadísticas en tiempo real (GET puntual + stream SSE): también con JWT.
                        // Si quieres restringir por rol: .hasRole("ADMIN") en lugar de .authenticated()
                        .requestMatchers("/api/estadisticas/**").authenticated()

                        // Todo lo demás exige autenticación
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Cache-Control",
                "Idempotency-Key", "If-Match"));
        config.setExposedHeaders(List.of("ETag", "Location", "Retry-After"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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