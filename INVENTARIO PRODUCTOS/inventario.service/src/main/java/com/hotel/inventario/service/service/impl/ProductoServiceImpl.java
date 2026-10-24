package com.hotel.inventario.service.service.impl;

import com.hotel.inventario.service.dto.DetalleVentaDto;
import com.hotel.inventario.service.dto.ProductoDto;
import com.hotel.inventario.service.entity.Producto;
import com.hotel.inventario.service.repository.ProductoRepository;
import com.hotel.inventario.service.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto buscarPorId(Integer id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public ProductoDto guardar(ProductoDto dto) {
        if (repository.findByNombre(dto.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto registrado con ese nombre.");
        }

        Producto producto = toEntity(dto);
        Producto productoGuardado = repository.save(producto);
        return toDto(productoGuardado);
    }

    @Override
    @Transactional
    public ProductoDto actualizar(Integer id, ProductoDto dto) {
        Producto productoExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado con ID: " + id));

        repository.findByNombre(dto.getNombre()).ifPresent(p -> {
            if (!p.getIdProducto().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro producto con ese nombre.");
            }
        });

        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDetalle(dto.getDetalle());
        productoExistente.setPrecio(dto.getPrecio());
        productoExistente.setCantidad(dto.getCantidad());
        productoExistente.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        productoExistente.setImagenUrl(dto.getImagenUrl());

        Producto productoActualizado = repository.save(productoExistente);
        return toDto(productoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Registro no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void descontarStock(List<DetalleVentaDto> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La lista de detalles de venta no puede estar vacía.");
        }

        for (DetalleVentaDto detalle : detalles) {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
            }

            Producto producto = repository.findById(detalle.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalle.getIdProducto()));

            if (Boolean.FALSE.equals(producto.getEstado())) {
                throw new IllegalStateException("El producto '" + producto.getNombre() + "' se encuentra inactivo para la venta.");
            }

            int stockActual = producto.getCantidad() != null ? producto.getCantidad() : 0;
            if (stockActual < detalle.getCantidad()) {
                throw new IllegalArgumentException(
                        String.format("Stock insuficiente para el producto '%s'. Stock actual: %d, Cantidad solicitada: %d",
                                producto.getNombre(), stockActual, detalle.getCantidad())
                );
            }

            producto.setCantidad(stockActual - detalle.getCantidad());
            repository.save(producto);
        }
    }

    @Override
    @Transactional
    public ProductoDto aumentarStock(Integer id, Integer cantidadIngreso) {
        if (cantidadIngreso == null || cantidadIngreso <= 0) {
            throw new IllegalArgumentException("La cantidad a ingresar debe ser mayor a cero. Valor recibido: " + cantidadIngreso);
        }

        Producto producto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado con ID: " + id));

        int stockActual = producto.getCantidad() != null ? producto.getCantidad() : 0;
        int stockFinal = stockActual + cantidadIngreso;

        producto.setCantidad(stockFinal);

        Producto productoActualizado = repository.save(producto);
        return toDto(productoActualizado);
    }

    private ProductoDto toDto(Producto e) {
        return ProductoDto.builder()
                .idProducto(e.getIdProducto())
                .nombre(e.getNombre())
                .detalle(e.getDetalle())
                .precio(e.getPrecio())
                .cantidad(e.getCantidad())
                .estado(e.getEstado())
                .imagenUrl(e.getImagenUrl())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    private Producto toEntity(ProductoDto d) {
        return Producto.builder()
                .idProducto(d.getIdProducto())
                .nombre(d.getNombre())
                .detalle(d.getDetalle())
                .precio(d.getPrecio())
                .cantidad(d.getCantidad() != null ? d.getCantidad() : 0)
                .estado(d.getEstado() == null || d.getEstado())
                .imagenUrl(d.getImagenUrl())
                .fechaCreacion(d.getFechaCreacion())
                .build();
    }
}