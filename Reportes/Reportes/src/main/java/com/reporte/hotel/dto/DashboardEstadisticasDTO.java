package com.reporte.hotel.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEstadisticasDTO {
    private LocalDateTime generadoEn;

    // --- Ventas / inventario ---
    private Integer ventasHoy;
    private BigDecimal ingresoVentasHoy;
    private Integer cantidadStockBajo;
    private List<ReporteProductoPopularDTO> topProductos;
    private List<ProductoDto> productosStockBajo;

    // --- Recepción / hospedaje ---
    private Integer estanciasActivas;          // recepciones con estado = true
    private Integer checkInsHoy;               // fechaEntrada = hoy
    private Integer salidasPrevistasHoy;       // activas con fechaSalida = hoy
    private Integer salidasHoy;                // cerradas hoy (fechaSalidaConfirmacion = hoy)
    private Integer cuentasPorCobrarCantidad;  // activas con saldo pendiente > 0
    private BigDecimal montoPorCobrar;         // suma de precioRestante de esas estancias
    private BigDecimal ingresoHospedajeHoy;    // suma de totalPagado de las cerradas hoy
    private Integer penalidadesHoyCantidad;
    private BigDecimal penalidadesHoyMonto;

    // --- Total ---
    private BigDecimal ingresoTotalHoy;        // ventas + hospedaje
}