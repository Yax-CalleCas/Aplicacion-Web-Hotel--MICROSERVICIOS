package com.auth.seguridad.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoPersonaDto {

    private Integer idTipoPersona;

    @NotBlank
    private String descripcion;

    private Boolean estado;

    private LocalDateTime fechaCreacion;
}