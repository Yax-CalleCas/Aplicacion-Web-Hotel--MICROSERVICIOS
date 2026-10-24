package com.hotel.inventario.service.service;

import com.hotel.inventario.service.dto.DetalleVentaDto;
import java.util.List;

public interface DetalleVentaService {
    List<DetalleVentaDto> listarTodos();
    DetalleVentaDto buscarPorId(Integer id);
    DetalleVentaDto guardar(DetalleVentaDto dto);
    DetalleVentaDto actualizar(Integer id, DetalleVentaDto dto);
    void eliminar(Integer id);
}