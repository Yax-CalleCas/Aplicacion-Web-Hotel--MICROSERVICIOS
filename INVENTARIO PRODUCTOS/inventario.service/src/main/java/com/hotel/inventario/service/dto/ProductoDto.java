package com.hotel.inventario.service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDto {
    private Integer idProducto;
    private String nombre;
    private String detalle;
    private BigDecimal precio;
    private Integer cantidad;
    private Boolean estado;
    private String imagenUrl;
    private LocalDateTime fechaCreacion;
}