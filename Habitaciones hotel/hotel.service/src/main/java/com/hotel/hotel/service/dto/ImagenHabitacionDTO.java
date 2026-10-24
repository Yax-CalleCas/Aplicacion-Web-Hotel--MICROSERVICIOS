package com.hotel.hotel.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagenHabitacionDTO {
    private Integer idImagen;
    private String urlImagen;
}