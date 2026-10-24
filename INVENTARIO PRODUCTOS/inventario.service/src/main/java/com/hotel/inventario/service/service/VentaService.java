package com.hotel.inventario.service.service;

import com.hotel.inventario.service.dto.VentaDto;
import java.util.List;

public interface VentaService {
    List<VentaDto> listarTodos();
    VentaDto buscarPorId(Integer id);
    VentaDto guardar(VentaDto dto);
    VentaDto actualizar(Integer id, VentaDto dto);
    void eliminar(Integer id);
    List<VentaDto> listarPorRecepcion(Integer idRecepcion);
}