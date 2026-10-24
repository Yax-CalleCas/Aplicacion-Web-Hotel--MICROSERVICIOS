package com.hotel.inventario.service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDto {
    private Integer idVenta;
    private Integer idRecepcion;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCreacion;
    private List<DetalleVentaDto> detalleVentas;
}