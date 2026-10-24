package com.hotel.inventario.service.controller;

import com.hotel.inventario.service.dto.ApiResponse;
import com.hotel.inventario.service.dto.VentaDto;
import com.hotel.inventario.service.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venta")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService service;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<VentaDto>>> listar() {
        List<VentaDto> lista = service.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(lista, "Listado de ventas obtenido correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaDto>> buscar(@PathVariable Integer id) {
        VentaDto venta = service.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(venta, "Venta encontrada con éxito"));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VentaDto>> registrar(@Valid @RequestBody VentaDto dto) {
        log.info("Registrando nueva venta asociada a la recepción ID: {}", dto.getIdRecepcion());
        VentaDto nuevaVenta = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(nuevaVenta, "Venta registrada con éxito"));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<VentaDto>> actualizar(@PathVariable Integer id, @Valid @RequestBody VentaDto dto) {
        log.info("Actualizando venta ID: {}", id);
        VentaDto ventaActualizada = service.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(ventaActualizada, "Venta actualizada correctamente"));
    }

    @GetMapping("/recepcion/{idRecepcion}")
    public ResponseEntity<ApiResponse<List<VentaDto>>> listarPorRecepcion(@PathVariable Integer idRecepcion) {
        List<VentaDto> lista = service.listarPorRecepcion(idRecepcion);
        return ResponseEntity.ok(ApiResponse.success(lista, "Ventas de la recepción obtenidas correctamente"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        log.info("Eliminando venta ID: {}", id);
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Venta eliminada correctamente"));
    }
}