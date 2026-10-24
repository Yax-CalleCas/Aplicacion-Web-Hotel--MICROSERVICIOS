package com.hotel.hotel.service.service;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    // Lista segura entre hilos: se recorre y modifica desde distintas peticiones
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter suscribirse() {
        // 0L = sin tiempo límite de expiración
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(emitter));

        try {
            // Abre el flujo de inmediato para que el navegador y el gateway lo den por activo
            emitter.send(SseEmitter.event().comment("conectado"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    @Scheduled(fixedRate = 15000)
    public void latido() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    public void notificarCambioHabitacion(Object habitacion) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("cambio-habitacion")
                        .data(habitacion, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}