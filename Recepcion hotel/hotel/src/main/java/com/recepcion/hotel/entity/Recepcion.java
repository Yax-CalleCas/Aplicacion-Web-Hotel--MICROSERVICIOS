package com.recepcion.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recepcion", indexes = {
        @Index(name = "idx_recepcion_habitacion_estado", columnList = "id_habitacion, estado"),
        @Index(name = "idx_recepcion_cliente", columnList = "id_cliente")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Integer idRecepcion;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_habitacion", nullable = false)
    private Integer idHabitacion;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "fecha_salida_confirmacion")
    private LocalDateTime fechaSalidaConfirmacion;

    @Column(name = "precio_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioInicial;

    @Column(name = "adelanto", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal adelanto = BigDecimal.ZERO;

    @Column(name = "precio_restante", precision = 10, scale = 2)
    private BigDecimal precioRestante;

    @Column(name = "total_pagado", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPagado = BigDecimal.ZERO;

    @Column(name = "costo_penalidad", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoPenalidad = BigDecimal.ZERO;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true; // true = Activo / En estancia, false = Finalizado / Check-out

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.adelanto == null) {
            this.adelanto = BigDecimal.ZERO;
        }
        if (this.totalPagado == null) {
            this.totalPagado = BigDecimal.ZERO;
        }
        if (this.costoPenalidad == null) {
            this.costoPenalidad = BigDecimal.ZERO;
        }
        if (this.estado == null) {
            this.estado = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}