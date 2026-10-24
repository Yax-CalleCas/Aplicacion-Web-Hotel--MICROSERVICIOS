package com.reporte.hotel.controller;

import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.RecepcionResponseDTO;
import com.reporte.hotel.service.ReporteRecepcionService;
import com.reporte.hotel.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/recepcion")
@RequiredArgsConstructor
public class ReporteRecepcionController {

    private final ReporteRecepcionService reporteRecepcionService;
    private final SseService sseService;

    @GetMapping("/alquileres")
    public ResponseEntity<ApiResponse<List<RecepcionResponseDTO>>> listarAlquileres(
            @RequestParam(required = false) Boolean estado) {
        List<RecepcionResponseDTO> resultado = reporteRecepcionService.obtenerReporteAlquileres(estado);
        return ResponseEntity.ok(ApiResponse.success(resultado, "Reporte de alquileres generado con éxito"));
    }

    @GetMapping("/cuentas-por-cobrar")
    public ResponseEntity<ApiResponse<List<RecepcionResponseDTO>>> listarCuentasPorCobrar() {
        List<RecepcionResponseDTO> resultado = reporteRecepcionService.obtenerCuentasPorCobrar();
        return ResponseEntity.ok(ApiResponse.success(resultado, "Reporte de cuentas por cobrar obtenido con éxito"));
    }

    @GetMapping("/penalidades")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerResumenPenalidades() {
        Map<String, Object> resultado = reporteRecepcionService.obtenerResumenPenalidades();
        return ResponseEntity.ok(ApiResponse.success(resultado, "Resumen de penalidades obtenido con éxito"));
    }

    // Endpoint SSE para actualizar el frontend en tiempo real sin recargar la página
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReportes() {
        return sseService.registrarCliente();
    }
}