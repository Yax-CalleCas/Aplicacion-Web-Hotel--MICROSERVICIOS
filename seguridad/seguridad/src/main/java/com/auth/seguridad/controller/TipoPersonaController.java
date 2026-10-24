package com.auth.seguridad.controller;

import com.auth.seguridad.dto.TipoPersonaDto;
import com.auth.seguridad.service.TipoPersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipopersona")
@RequiredArgsConstructor
public class TipoPersonaController {

    private final TipoPersonaService service;

    @GetMapping("/listar")
    public ResponseEntity<List<TipoPersonaDto>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<TipoPersonaDto> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/registrar")
    public ResponseEntity<TipoPersonaDto> registrar(@Valid @RequestBody TipoPersonaDto dto) {
        return ResponseEntity.ok(service.guardar(dto));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<TipoPersonaDto> actualizar(@PathVariable Integer id, @Valid @RequestBody TipoPersonaDto dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}