package com.reporte.hotel.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteOcupacionDTO {
    private Integer idSede;
    private LocalDate fecha;
    private Integer totalHabitaciones;
    private Integer habitacionesOcupadas;
    private Integer habitacionesLibres;
    private BigDecimal porcentajeOcupacion;
}