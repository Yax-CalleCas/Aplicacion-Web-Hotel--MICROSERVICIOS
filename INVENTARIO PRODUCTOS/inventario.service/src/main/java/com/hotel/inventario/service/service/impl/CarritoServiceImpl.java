package com.hotel.inventario.service.service.impl;

import com.hotel.inventario.service.dto.CarritoDto;
import com.hotel.inventario.service.entity.Carrito;
import com.hotel.inventario.service.repository.CarritoRepository;
import com.hotel.inventario.service.repository.ProductoRepository;
import com.hotel.inventario.service.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public Boolean agregarProducto(Integer idRecepcion, Integer idProducto, Integer cantidad) {
        var producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("El producto no existe"));

        Optional<Carrito> existente = carritoRepository.findByIdRecepcionAndProducto_IdProducto(idRecepcion, idProducto);

        if (existente.isPresent()) {
            Carrito carrito = existente.get();
            carrito.setCantidad(carrito.getCantidad() + cantidad);
            carritoRepository.save(carrito);
        } else {
            Carrito carrito = Carrito.builder()
                    .idRecepcion(idRecepcion)
                    .producto(producto)
                    .cantidad(cantidad)
                    .build();
            carritoRepository.save(carrito);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarritoDto> obtenerCarritoPorRecepcion(Integer idRecepcion) {
        return carritoRepository.findByIdRecepcion(idRecepcion)
                .stream()
                .map(c -> {
                    BigDecimal precio = c.getProducto().getPrecio() != null ? c.getProducto().getPrecio() : BigDecimal.ZERO;
                    BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(c.getCantidad()));
                    return CarritoDto.builder()
                            .idCarrito(c.getIdCarrito())
                            .idRecepcion(c.getIdRecepcion())
                            .idProducto(c.getProducto().getIdProducto())
                            .nombreProducto(c.getProducto().getNombre())
                            .precioProducto(precio)
                            .cantidad(c.getCantidad())
                            .subtotal(subtotal)
                            .fechaRegistro(c.getFechaRegistro())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer idRecepcion, Integer idProducto) {
        carritoRepository.deleteByIdRecepcionAndProducto_IdProducto(idRecepcion, idProducto);
    }
}