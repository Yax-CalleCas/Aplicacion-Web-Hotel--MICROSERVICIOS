package com.hotel.inventario.service.controller;

import com.hotel.inventario.service.dto.ApiResponse;
import com.hotel.inventario.service.dto.CarritoDto;
import com.hotel.inventario.service.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping("/agregar")
    public ResponseEntity<ApiResponse<Boolean>> agregarProducto(
            @RequestParam Integer idRecepcion,
            @RequestParam Integer idProducto,
            @RequestParam Integer cantidad) {
        log.info("Agregando producto ID: {} (Cant: {}) a la recepción ID: {}", idProducto, cantidad, idRecepcion);
        Boolean resultado = carritoService.agregarProducto(idRecepcion, idProducto, cantidad);
        return ResponseEntity.ok(ApiResponse.success(resultado, "Producto agregado al carrito correctamente"));
    }

    @GetMapping("/listar/{idRecepcion}")
    public ResponseEntity<ApiResponse<List<CarritoDto>>> obtenerCarrito(@PathVariable Integer idRecepcion) {
        List<CarritoDto> carrito = carritoService.obtenerCarritoPorRecepcion(idRecepcion);
        return ResponseEntity.ok(ApiResponse.success(carrito, "Carrito obtenido correctamente para la recepción"));
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<ApiResponse<Void>> eliminarProducto(
            @RequestParam Integer idRecepcion,
            @RequestParam Integer idProducto) {
        log.info("Eliminando producto ID: {} del carrito de la recepción ID: {}", idProducto, idRecepcion);
        carritoService.eliminarProducto(idRecepcion, idProducto);
        return ResponseEntity.ok(ApiResponse.success(null, "Producto removido del carrito correctamente"));
    }
}