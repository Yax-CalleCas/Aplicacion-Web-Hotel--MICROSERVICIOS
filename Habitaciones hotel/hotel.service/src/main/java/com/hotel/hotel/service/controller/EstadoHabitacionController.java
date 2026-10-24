package com.hotel.hotel.service.controller;

import com.hotel.hotel.service.dto.ApiResponse;
import com.hotel.hotel.service.dto.EstadoHabitacionRequestDTO;
import com.hotel.hotel.service.dto.EstadoHabitacionResponseDTO;
import com.hotel.hotel.service.service.EstadoHabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadohabitacion")
@RequiredArgsConstructor
public class EstadoHabitacionController {

    private final EstadoHabitacionService service;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<EstadoHabitacionResponseDTO>>> listar() {
        List<EstadoHabitacionResponseDTO> lista = service.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(lista));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<EstadoHabitacionResponseDTO>> buscar(@PathVariable Integer id) {
        EstadoHabitacionResponseDTO dto = service.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<EstadoHabitacionResponseDTO>> registrar(@Valid @RequestBody EstadoHabitacionRequestDTO dto) {
        EstadoHabitacionResponseDTO nuevo = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(nuevo));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<EstadoHabitacionResponseDTO>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EstadoHabitacionRequestDTO dto) {
        EstadoHabitacionResponseDTO actualizado = service.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(actualizado));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}