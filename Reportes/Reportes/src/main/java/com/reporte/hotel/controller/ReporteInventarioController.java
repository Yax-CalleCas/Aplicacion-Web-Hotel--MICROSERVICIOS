package com.reporte.hotel.controller;


import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.ProductoDto;
import com.reporte.hotel.service.ReporteInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/inventario")
@RequiredArgsConstructor
public class ReporteInventarioController {

    private final ReporteInventarioService reporteInventarioService;

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarMasVendidos() {
        List<Map<String, Object>> resultado = reporteInventarioService.obtenerProductosMasVendidos();
        return ResponseEntity.ok(ApiResponse.success(resultado, "Reporte de productos más vendidos generado con éxito"));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<ProductoDto>>> listarStockBajo(@RequestParam(defaultValue = "5") int umbral) {
        List<ProductoDto> resultado = reporteInventarioService.obtenerProductosStockBajo(umbral);
        return ResponseEntity.ok(ApiResponse.success(resultado, "Reporte de stock bajo obtenido con éxito"));
    }
}