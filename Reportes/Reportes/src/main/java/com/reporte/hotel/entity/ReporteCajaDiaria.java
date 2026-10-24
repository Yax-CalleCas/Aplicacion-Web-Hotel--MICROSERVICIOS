package com.reporte.hotel.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reporte_caja_diaria")
public class ReporteCajaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte_caja")
    private Integer idReporteCaja;

    @Column(name = "id_sede", nullable = false)
    private Integer idSede;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @Column(name = "total_hospedaje", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalHospedaje = BigDecimal.ZERO;

    @Column(name = "total_ventas_inventario", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalVentasInventario = BigDecimal.ZERO;

    @Column(name = "total_penalidades", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPenalidades = BigDecimal.ZERO;

    @Column(name = "ingreso_total", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal ingresoTotal = BigDecimal.ZERO;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaGeneracion == null) {
            this.fechaGeneracion = LocalDateTime.now();
        }
    }
}