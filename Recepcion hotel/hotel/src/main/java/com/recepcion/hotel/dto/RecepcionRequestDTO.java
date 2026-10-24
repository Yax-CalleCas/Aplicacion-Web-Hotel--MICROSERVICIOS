package com.recepcion.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idCliente;

    @NotNull(message = "El ID de la habitación es obligatorio")
    private Integer idHabitacion;

    @NotNull(message = "La fecha de entrada es obligatoria")
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDate fechaSalida;

    @NotNull(message = "El precio inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio inicial debe ser mayor a cero")
    private BigDecimal precioInicial;

    @DecimalMin(value = "0.0", message = "El adelanto no puede ser negativo")
    private BigDecimal adelanto;

    private String observacion;
}