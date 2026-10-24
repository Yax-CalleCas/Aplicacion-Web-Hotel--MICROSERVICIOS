package com.reporte.hotel.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reporte_ocupacion")
public class ReporteOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte_ocupacion")
    private Integer idReporteOcupacion;

    @Column(name = "id_sede", nullable = false)
    private Integer idSede;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "total_habitaciones", nullable = false)
    private Integer totalHabitaciones;

    @Column(name = "habitaciones_ocupadas", nullable = false)
    private Integer habitacionesOcupadas;

    @Column(name = "habitaciones_libres", nullable = false)
    private Integer habitacionesLibres;

    @Column(name = "porcentaje_ocupacion", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal porcentajeOcupacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}