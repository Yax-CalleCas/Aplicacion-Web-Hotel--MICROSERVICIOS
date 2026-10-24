package com.hotel.hotel.service.controller;

import com.hotel.hotel.service.dto.ApiResponse;
import com.hotel.hotel.service.dto.PisoRequestDTO;
import com.hotel.hotel.service.service.PisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/piso")
@RequiredArgsConstructor
public class PisoController {
    private final PisoService service;

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
    public ResponseEntity<ApiResponse<?>> registrar(@Valid @RequestBody PisoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.guardar(dto)));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<?>> actualizar(@PathVariable Integer id, @Valid @RequestBody PisoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.actualizar(id, dto)));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<?>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}