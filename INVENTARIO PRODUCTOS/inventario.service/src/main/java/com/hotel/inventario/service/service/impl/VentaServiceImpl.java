package com.hotel.inventario.service.service.impl;

import com.hotel.inventario.service.dto.DetalleVentaDto;
import com.hotel.inventario.service.dto.VentaDto;
import com.hotel.inventario.service.entity.DetalleVenta;
import com.hotel.inventario.service.entity.Producto;
import com.hotel.inventario.service.entity.Venta;
import com.hotel.inventario.service.repository.ProductoRepository;
import com.hotel.inventario.service.repository.VentaRepository;
import com.hotel.inventario.service.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository repository;
    private final ProductoRepository productoRepository;
    private final StreamBridge streamBridge; // Inyectado para publicar eventos de ventas a RabbitMQ

    @Override
    @Transactional(readOnly = true)
    public List<VentaDto> listarTodos() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDto buscarPorId(Integer id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VentaDto guardar(VentaDto dto) {
        if (dto.getIdRecepcion() == null || dto.getDetalleVentas() == null || dto.getDetalleVentas().isEmpty()) {
            throw new IllegalArgumentException("Datos de venta incompletos: se requiere la recepción y al menos un detalle de producto.");
        }

        Venta venta = new Venta();
        venta.setIdRecepcion(dto.getIdRecepcion());
        venta.setEstado(dto.getEstado() != null ? dto.getEstado() : "COMPLETADA");

        BigDecimal totalVenta = BigDecimal.ZERO;

        List<DetalleVenta> detalles = dto.getDetalleVentas().stream().map(d -> {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + d.getIdProducto()));

            if (producto.getCantidad() == null || producto.getCantidad() < d.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (Stock disponible: " + (producto.getCantidad() != null ? producto.getCantidad() : 0) + ")");
            }

            producto.setCantidad(producto.getCantidad() - d.getCantidad());
            productoRepository.save(producto);

            BigDecimal precioUnitario = producto.getPrecio() != null ? producto.getPrecio() : BigDecimal.ZERO;
            BigDecimal subTotal = precioUnitario.multiply(BigDecimal.valueOf(d.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(d.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubTotal(subTotal);
            return detalle;
        }).collect(Collectors.toList());

        for (DetalleVenta det : detalles) {
            totalVenta = totalVenta.add(det.getSubTotal());
        }

        venta.setTotal(totalVenta);
        venta.setDetalleVentas(detalles);

        Venta ventaGuardada = repository.save(venta);
        VentaDto resultadoDto = toDto(ventaGuardada);

        // Notificar por RabbitMQ la creación de la venta
        publicarEventoVenta("VENTA_REGISTRADA", resultadoDto);

        return resultadoDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDto> listarPorRecepcion(Integer idRecepcion) {
        if (idRecepcion == null) return Collections.emptyList();
        return repository.findByIdRecepcion(idRecepcion).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VentaDto actualizar(Integer id, VentaDto dto) {
        Venta venta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (dto.getEstado() != null) {
            venta.setEstado(dto.getEstado());
        }

        Venta ventaActualizada = repository.save(venta);
        VentaDto resultadoDto = toDto(ventaActualizada);

        // Notificar por RabbitMQ la actualización de la venta
        publicarEventoVenta("VENTA_ACTUALIZADA", resultadoDto);

        return resultadoDto;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Venta no encontrada con ID: " + id);
        }
        repository.deleteById(id);

        // Notificar por RabbitMQ la eliminación de la venta
        try {
            streamBridge.send("ventaEventOut-out-0", Collections.singletonMap("idVenta", id));
        } catch (Exception e) {
            log.error("No se pudo publicar el evento de eliminación de venta {}: {}", id, e.getMessage());
        }
    }

    private void publicarEventoVenta(String tipoEvento, VentaDto ventaDto) {
        try {
            boolean enviado = streamBridge.send("ventaEventOut-out-0", ventaDto);
            if (enviado) {
                log.info("Evento de venta publicado por RabbitMQ con éxito: tipo={}, idVenta={}", tipoEvento, ventaDto.getIdVenta());
            } else {
                log.warn("El binder de RabbitMQ no pudo despachar el evento de venta idVenta={}", ventaDto.getIdVenta());
            }
        } catch (Exception e) {
            log.error("Error al enviar evento de venta por RabbitMQ: {}", e.getMessage(), e);
        }
    }

    private VentaDto toDto(Venta venta) {
        List<DetalleVentaDto> detallesDto = venta.getDetalleVentas() != null ? venta.getDetalleVentas().stream().map(d ->
                DetalleVentaDto.builder()
                        .idDetalleVenta(d.getIdDetalleVenta())
                        .idVenta(venta.getIdVenta())
                        .idProducto(d.getProducto() != null ? d.getProducto().getIdProducto() : null)
                        .nombreProducto(d.getProducto() != null ? d.getProducto().getNombre() : "Sin Nombre")
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subTotal(d.getSubTotal())
                        .build()
        ).collect(Collectors.toList()) : Collections.emptyList();

        return VentaDto.builder()
                .idVenta(venta.getIdVenta())
                .idRecepcion(venta.getIdRecepcion())
                .total(venta.getTotal())
                .estado(venta.getEstado())
                .fechaCreacion(venta.getFechaCreacion())
                .detalleVentas(detallesDto)
                .build();
    }
}