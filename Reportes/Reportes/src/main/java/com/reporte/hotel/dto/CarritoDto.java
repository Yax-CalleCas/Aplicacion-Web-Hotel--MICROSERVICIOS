package com.reporte.hotel.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDto {
    private Integer idCarrito;
    private Integer idRecepcion;
    private Integer idProducto;
    private String nombreProducto;
    private BigDecimal precioProducto;
    private Integer cantidad;
    private BigDecimal subtotal;
    private LocalDateTime fechaRegistro;
}