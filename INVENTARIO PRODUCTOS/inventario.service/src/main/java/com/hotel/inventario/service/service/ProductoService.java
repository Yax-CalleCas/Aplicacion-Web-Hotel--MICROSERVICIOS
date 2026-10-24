package com.hotel.inventario.service.service;

import com.hotel.inventario.service.dto.DetalleVentaDto;
import com.hotel.inventario.service.dto.ProductoDto;
import java.util.List;

public interface ProductoService {
    List<ProductoDto> listarTodos();
    ProductoDto buscarPorId(Integer id);
    ProductoDto guardar(ProductoDto dto);
    ProductoDto actualizar(Integer id, ProductoDto dto);
    void descontarStock(List<DetalleVentaDto> detalles);
    ProductoDto aumentarStock(Integer id, Integer cantidadIngreso);
    void eliminar(Integer id);
}