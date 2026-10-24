package com.reporte.hotel.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteProductoPopularDTO {
    private Integer idProducto;
    private String nombreProducto;
    private Integer cantidadVendida;
    private BigDecimal ingresoGenerado;
    private LocalDate fechaCorte;
}