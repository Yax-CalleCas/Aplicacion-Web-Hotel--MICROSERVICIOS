package com.auth.seguridad.controller;

import com.auth.seguridad.dto.LoginRequestDto;
import com.auth.seguridad.dto.LoginResponseDto;
import com.auth.seguridad.dto.SedeDto;
import com.auth.seguridad.entity.Persona;
import com.auth.seguridad.exception.BusinessException;
import com.auth.seguridad.repository.PersonaRepository;
import com.auth.seguridad.security.JwtService;
import com.auth.seguridad.security.LoginAttemptService; // <-- Importamos nuestro servicio de intentos
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class LoginController {

    private final PersonaRepository personaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {

        String correo = request.getCorreo();

        // 0. Validación previa de bloqueo por intentos fallidos
        if (loginAttemptService.isBlocked(correo)) {
            long minutosRestantes = loginAttemptService.getRemainingLockTimeInMinutes(correo);
            throw new BusinessException("Cuenta bloqueada temporalmente por seguridad debido a múltiples intentos fallidos. Intente de nuevo en " + minutosRestantes + " minuto(s).");
        }

        // 1. Validar que el usuario exista trayendo el rol y las sedes con JOIN FETCH
        Persona persona = personaRepository
                .findByCorreoWithTipo(correo)
                .orElseThrow(() -> {
                    // Opcional: registrar fallo también si el correo no existe, o manejarlo directo
                    return new BusinessException("Correo no registrado");
                });

        // 2. Validar que el usuario esté activo
        if (persona.getEstado() != null && !persona.getEstado()) {
            throw new BusinessException("Usuario inactivo, acceso denegado");
        }

        // 3. Validar contraseña cifrada
        if (persona.getClave() == null || !passwordEncoder.matches(request.getClave(), persona.getClave())) {
            // Registramos el intento fallido en el servicio de seguridad
            loginAttemptService.loginFailed(correo);
            throw new BusinessException("Contraseña incorrecta");
        }

        // 4. Si el login es exitoso, limpiamos el contador de intentos fallidos
        loginAttemptService.loginSucceeded(correo);

        // 5. Obtención y validación defensiva del Rol
        String rolDescripcion = "Cliente";
        if (persona.getTipoPersona() != null && persona.getTipoPersona().getDescripcion() != null) {
            rolDescripcion = persona.getTipoPersona().getDescripcion();
        }

        // 6. Generar Token JWT INCLUYENDO el rol en los claims
        String token = jwtService.generateToken(persona.getCorreo(), rolDescripcion);

        // 7. Mapear sedes asociadas al DTO de respuesta
        Set<SedeDto> sedesDto = persona.getSedes() != null ? persona.getSedes().stream().map(s ->
                SedeDto.builder()
                        .idSede(s.getIdSede())
                        .codigo(s.getCodigo())
                        .nombre(s.getNombre())
                        .estado(s.getEstado())
                        .fechaCreacion(s.getFechaCreacion())
                        .build()
        ).collect(Collectors.toSet()) : new HashSet<>();

        // 8. Construir DTO de respuesta incluyendo las sedes autorizadas
        LoginResponseDto response = LoginResponseDto.builder()
                .idPersona(persona.getIdPersona())
                .nombre(persona.getNombre())
                .apellido(persona.getApellido())
                .correo(persona.getCorreo())
                .tipoPersona(rolDescripcion)
                .sedes(sedesDto)
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }
}