package com.hotel.hotel.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PisoResponseDTO {
    private Integer idPiso;
    private Integer idHotel;
    private String descripcion;
    private Boolean estado;
}