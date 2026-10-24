package com.reporte.hotel.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteCajaDiariaDTO {
    private Integer idSede;
    private LocalDate fechaOperacion;
    private BigDecimal totalHospedaje;
    private BigDecimal totalVentasInventario;
    private BigDecimal totalPenalidades;
    private BigDecimal ingresoTotal;
}