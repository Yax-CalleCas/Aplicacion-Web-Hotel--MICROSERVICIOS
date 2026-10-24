package com.hotel.inventario.service.controller;

import com.hotel.inventario.service.dto.ApiResponse;
import com.hotel.inventario.service.dto.ProductoDto;
import com.hotel.inventario.service.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producto")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService service;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<ProductoDto>>> listar() {
        List<ProductoDto> lista = service.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(lista, "Listado de productos obtenido correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDto>> buscar(@PathVariable Integer id) {
        ProductoDto producto = service.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(producto, "Producto encontrado con éxito"));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<ProductoDto>> registrar(@Valid @RequestBody ProductoDto dto) {
        log.info("Registrando nuevo producto: {}", dto.getNombre());
        ProductoDto creado = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creado, "Producto registrado con éxito"));
    }

    @PatchMapping("/{id}/stock/aumentar")
    public ResponseEntity<ApiResponse<ProductoDto>> aumentarStock(
            @PathVariable Integer id,
            @RequestParam Integer cantidad) {
        log.info("Aumentando stock en {} unidades para el producto ID: {}", cantidad, id);
        // Corregido: se usa 'service' en lugar de 'productoService'
        ProductoDto productoActualizado = service.aumentarStock(id, cantidad);
        return ResponseEntity.ok(ApiResponse.success(productoActualizado, "Stock incrementado correctamente"));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<ProductoDto>> actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoDto dto) {
        log.info("Actualizando producto ID: {}", id);
        ProductoDto actualizado = service.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(actualizado, "Producto actualizado correctamente"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        log.info("Eliminando producto ID: {}", id);
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado correctamente"));
    }
}