package com.hotel.hotel.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoHabitacionResponseDTO {
    private Integer idEstadoHabitacion;
    private String codigo;
    private String descripcion;
    private Boolean estado;
}