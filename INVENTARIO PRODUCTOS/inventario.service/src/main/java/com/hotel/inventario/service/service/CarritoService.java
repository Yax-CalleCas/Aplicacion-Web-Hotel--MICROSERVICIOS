package com.hotel.inventario.service.service;

import com.hotel.inventario.service.dto.CarritoDto;
import java.util.List;

public interface CarritoService {
    Boolean agregarProducto(Integer idRecepcion, Integer idProducto, Integer cantidad);
    List<CarritoDto> obtenerCarritoPorRecepcion(Integer idRecepcion);
    void eliminarProducto(Integer idRecepcion, Integer idProducto);
}