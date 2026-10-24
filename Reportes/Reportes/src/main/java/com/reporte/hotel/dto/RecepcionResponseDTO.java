package com.reporte.hotel.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionResponseDTO {

    private Integer idRecepcion;
    private Integer idCliente;
    private Integer idHabitacion;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private LocalDateTime fechaSalidaConfirmacion;
    private BigDecimal precioInicial;
    private BigDecimal adelanto;
    private BigDecimal precioRestante;
    private BigDecimal totalPagado;
    private BigDecimal costoPenalidad;
    private String observacion;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}