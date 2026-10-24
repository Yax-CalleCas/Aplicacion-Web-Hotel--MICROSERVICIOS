package com.reporte.hotel.controller;

import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.DashboardEstadisticasDTO;
import com.reporte.hotel.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService estadisticasService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEstadisticas() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            DashboardEstadisticasDTO ultimo = estadisticasService.obtenerUltimo();
            if (ultimo != null) {
                emitter.send(SseEmitter.event().name("init").data(ultimo));
            }
        } catch (IOException | IllegalStateException e) {
            log.warn("Error al enviar el snapshot inicial: {}", e.getMessage());
            emitter.complete();
            emitters.remove(emitter);
        }

        return emitter;
    }

    @GetMapping
    public ApiResponse<DashboardEstadisticasDTO> obtenerSnapshot() {
        DashboardEstadisticasDTO ultimo = estadisticasService.obtenerUltimo();
        return ultimo != null
                ? ApiResponse.success(ultimo)
                : ApiResponse.success(null, "Aún no hay estadísticas calculadas");
    }

    @EventListener
    public void handleEstadisticasEvent(DashboardEstadisticasDTO snapshot) {
        if (snapshot == null) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("update").data(snapshot));
            } catch (IOException | IllegalStateException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}