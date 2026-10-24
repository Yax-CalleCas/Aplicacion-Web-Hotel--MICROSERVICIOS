package com.hotel.hotel.service.controller;

import com.hotel.hotel.service.dto.ApiResponse;
import com.hotel.hotel.service.dto.CategoriaResponseDTO;
import com.hotel.hotel.service.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categoria")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService service;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<?>> listar() {
        return ResponseEntity.ok(ApiResponse.success(service.listarTodos()));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<?>> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<?>> registrar(@Valid @RequestBody CategoriaResponseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.guardar(dto)));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<?>> actualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaResponseDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.actualizar(id, dto)));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<?>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}