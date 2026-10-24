package com.reporte.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitacionEventDTO {
    private String eventType;
    private Integer idHabitacion;
    private Integer idHotel;
    private String nuevoEstado;
    private LocalDateTime timestamp;
}