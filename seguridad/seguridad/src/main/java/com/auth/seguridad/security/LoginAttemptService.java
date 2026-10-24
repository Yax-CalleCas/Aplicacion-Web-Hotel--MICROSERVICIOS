package com.auth.seguridad.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_MINUTES = 3;

    // Estructuras concurrentes en memoria para almacenar intentos y bloqueos por correo
    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lockTimeCache = new ConcurrentHashMap<>();

    // Registrar un fallo de login
    public void loginFailed(String correo) {
        int attempts = attemptsCache.getOrDefault(correo, 0) + 1;
        attemptsCache.put(correo, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockTimeCache.put(correo, LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
        }
    }

    // Limpiar intentos si el login es exitoso
    public void loginSucceeded(String correo) {
        attemptsCache.remove(correo);
        lockTimeCache.remove(correo);
    }

    // Verificar si el correo está bloqueado actualmente
    public boolean isBlocked(String correo) {
        LocalDateTime lockTime = lockTimeCache.get(correo);
        if (lockTime == null) {
            return false;
        }

        // Si ya pasaron los 3 minutos, desbloqueamos automáticamente
        if (lockTime.isBefore(LocalDateTime.now())) {
            loginSucceeded(correo);
            return false;
        }

        return true;
    }

    // Obtener los minutos restantes de bloqueo para informar al usuario
    public long getRemainingLockTimeInMinutes(String correo) {
        LocalDateTime lockTime = lockTimeCache.get(correo);
        if (lockTime == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), lockTime).toMinutes() + 1;
    }
}