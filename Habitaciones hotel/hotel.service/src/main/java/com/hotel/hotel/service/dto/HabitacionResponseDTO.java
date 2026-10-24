package com.hotel.hotel.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionResponseDTO {
    private Integer idHabitacion;
    private Integer idHotel;
    private String numero;
    private String detalle;
    private BigDecimal precio;
    private EstadoHabitacionResponseDTO estadoHabitacion;
    private PisoResponseDTO piso;
    private CategoriaResponseDTO categoria;
    private List<ImagenHabitacionDTO> imagenes;
    private Boolean estado;
}