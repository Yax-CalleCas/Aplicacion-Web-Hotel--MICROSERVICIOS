package com.auth.seguridad.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeDto {

    private Integer idSede;

    @NotBlank(message = "El código de la sede es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String nombre;

    private Boolean estado;

    private LocalDateTime fechaCreacion;
}