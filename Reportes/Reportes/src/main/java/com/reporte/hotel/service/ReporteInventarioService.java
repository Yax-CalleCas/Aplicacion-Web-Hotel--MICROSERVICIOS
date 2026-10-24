package com.reporte.hotel.service;


import com.reporte.hotel.client.InventarioVentaClient;
import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.DetalleVentaDto;
import com.reporte.hotel.dto.ProductoDto;
import com.reporte.hotel.dto.VentaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteInventarioService {

    private final InventarioVentaClient inventarioVentaClient;

    // 1. Reporte de productos más vendidos y total recaudado por producto
    public List<Map<String, Object>> obtenerProductosMasVendidos() {
        ApiResponse<List<VentaDto>> response = inventarioVentaClient.listarVentas();

        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("No se pudieron obtener las ventas desde el servicio de inventario.");
        }

        List<VentaDto> ventas = response.getData();

        // Extraer todos los detalles de venta de todas las transacciones
        List<DetalleVentaDto> todosLosDetalles = ventas.stream()
                .filter(v -> "COMPLETADA".equalsIgnoreCase(v.getEstado()) || v.getEstado() == null) // Opcional: filtrar estados válidos
                .flatMap(v -> v.getDetalleVentas().stream())
                .collect(Collectors.toList());

        // Agrupar por idProducto y sumar cantidades y sub-totales
        Map<Integer, Map<String, Object>> resumenMap = todosLosDetalles.stream()
                .collect(Collectors.groupingBy(
                        DetalleVentaDto::getIdProducto,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    String nombre = list.get(0).getNombreProducto();
                                    int totalCantidad = list.stream().mapToInt(DetalleVentaDto::getCantidad).sum();
                                    BigDecimal totalIngreso = list.stream()
                                            .map(DetalleVentaDto::getSubTotal)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    return Map.of(
                                            "idProducto", list.get(0).getIdProducto(),
                                            "nombreProducto", nombre != null ? nombre : "Producto Desconocido",
                                            "cantidadVendida", totalCantidad,
                                            "ingresoGenerado", totalIngreso
                                    );
                                }
                        )
                ));

        // Ordenar de mayor a menor según la cantidad vendida
        return resumenMap.values().stream()
                .sorted((a, b) -> ((Integer) b.get("cantidadVendida")).compareTo((Integer) a.get("cantidadVendida")))
                .collect(Collectors.toList());
    }

    // 2. Reporte de productos con stock bajo (ej. menos de 5 unidades)
    public List<ProductoDto> obtenerProductosStockBajo(int umbral) {
        ApiResponse<List<ProductoDto>> response = inventarioVentaClient.listarProductos();

        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("No se pudieron obtener los productos.");
        }

        return response.getData().stream()
                .filter(p -> p.getCantidad() != null && p.getCantidad() <= umbral)
                .sorted(Comparator.comparingInt(ProductoDto::getCantidad))
                .collect(Collectors.toList());
    }
}