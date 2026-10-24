package com.reporte.hotel.client;

import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.ProductoDto;
import com.reporte.hotel.dto.VentaDto;

import com.reporte.hotel.security.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "inventario-productos", configuration = FeignClientConfig.class) // 👈 Agrégalo aquí
public interface InventarioVentaClient {

    @GetMapping("/api/venta/listar")
    ApiResponse<List<VentaDto>> listarVentas();

    @GetMapping("/api/producto/listar")
    ApiResponse<List<ProductoDto>> listarProductos();
}