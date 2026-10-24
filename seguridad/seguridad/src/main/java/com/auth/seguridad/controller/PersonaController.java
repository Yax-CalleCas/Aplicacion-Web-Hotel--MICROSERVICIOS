package com.auth.seguridad.controller;

import com.auth.seguridad.dto.PersonaDto;
import com.auth.seguridad.dto.RegistroClienteDto;
import com.auth.seguridad.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persona")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService service;

    @GetMapping("/listar")
    public ResponseEntity<List<PersonaDto>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<PersonaDto> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/registrar")
    public ResponseEntity<PersonaDto> registrar(@Valid @RequestBody PersonaDto dto) {
        PersonaDto nuevaPersona = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPersona);
    }

    @PostMapping("/registro-publico")
    public ResponseEntity<PersonaDto> registroPublico(@Valid @RequestBody RegistroClienteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCliente(dto));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<PersonaDto> actualizar(@PathVariable Integer id, @Valid @RequestBody PersonaDto dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok("Persona eliminada correctamente");
    }
}