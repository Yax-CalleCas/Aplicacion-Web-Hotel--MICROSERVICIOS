package com.hotel.hotel.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PisoRequestDTO {
    @NotNull(message = "El ID del hotel/sede es obligatorio")
    private Integer idHotel;

    @NotBlank(message = "La descripción del piso es obligatoria")
    private String descripcion;

    private Boolean estado;
}