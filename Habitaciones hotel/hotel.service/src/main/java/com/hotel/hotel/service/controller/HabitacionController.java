package com.hotel.hotel.service.controller;

import com.hotel.hotel.service.dto.ApiResponse;
import com.hotel.hotel.service.dto.HabitacionRequestDTO;
import com.hotel.hotel.service.dto.HabitacionResponseDTO;
import com.hotel.hotel.service.service.HabitacionService;
import com.hotel.hotel.service.service.SseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/habitacion")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService service;
    private final SseService sseService; // <-- Inyectar el servicio SSE

    // --- NUEVO ENDPOINT PARA SSE ---
    @GetMapping("/stream")
    public SseEmitter streamHabitaciones() {
        return sseService.suscribirse();
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<?>> listar() {
        return ResponseEntity.ok(ApiResponse.success(service.listarTodos()));
    }

    @GetMapping("/listar/hotel/{idHotel}")
    public ResponseEntity<ApiResponse<?>> listarPorHotel(@PathVariable Integer idHotel) {
        return ResponseEntity.ok(ApiResponse.success(service.listarPorHotel(idHotel)));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<?>> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<?>> registrar(@Valid @RequestBody HabitacionRequestDTO dto) {
        HabitacionResponseDTO creada = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(creada));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<?>> actualizar(@PathVariable Integer id, @Valid @RequestBody HabitacionRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.actualizar(id, dto)));
    }

    @PatchMapping("/{id}/estado/{idEstado}")
    public ResponseEntity<ApiResponse<?>> actualizarEstado(
            @PathVariable Integer id,
            @PathVariable Integer idEstado) {
        HabitacionResponseDTO actualizada = service.actualizarEstado(id, idEstado);
        return ResponseEntity.ok(ApiResponse.success(actualizada));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<?>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}