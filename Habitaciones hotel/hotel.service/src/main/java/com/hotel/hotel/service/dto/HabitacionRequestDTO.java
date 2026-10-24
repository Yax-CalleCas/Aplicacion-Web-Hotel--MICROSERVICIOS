package com.hotel.hotel.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data

@AllArgsConstructor
@NoArgsConstructor
public class HabitacionRequestDTO {
    @NotNull(message = "El ID del hotel/sede es obligatorio")
    private Integer idHotel;

    @NotBlank(message = "El número de habitación es obligatorio")
    private String numero;

    private String detalle;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "El estado de la habitación es obligatorio")
    private Integer idEstadoHabitacion;

    @NotNull(message = "El piso es obligatorio")
    private Integer idPiso;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    private List<String> urlsImagenes;
}