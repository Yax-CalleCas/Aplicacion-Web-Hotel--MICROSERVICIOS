package com.auth.seguridad.controller;

import com.auth.seguridad.dto.SedeDto;
import com.auth.seguridad.service.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sede")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @GetMapping("/listar")
    public ResponseEntity<List<SedeDto>> listar() {
        return ResponseEntity.ok(sedeService.listarTodas());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<SedeDto> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(sedeService.buscarPorId(id));
    }
}