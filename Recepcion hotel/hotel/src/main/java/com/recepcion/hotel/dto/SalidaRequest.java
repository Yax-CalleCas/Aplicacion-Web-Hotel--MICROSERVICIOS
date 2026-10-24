package com.recepcion.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalidaRequest {

    @NotNull(message = "El ID de la recepción es obligatorio")
    private Integer idRecepcion;

    @DecimalMin(value = "0.0", message = "El costo de penalidad no puede ser negativo")
    private BigDecimal costoPenalidad;

    private String observacion;
}