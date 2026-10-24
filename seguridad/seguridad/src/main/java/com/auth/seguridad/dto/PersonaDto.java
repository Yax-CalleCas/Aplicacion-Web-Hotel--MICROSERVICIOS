package com.auth.seguridad.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaDto {

    private Integer idPersona;

    @NotBlank
    private String tipoDocumento;

    @NotBlank
    private String documento;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @Email
    private String correo;

    private String fotoUrl;

    private String clave;

    @NotNull
    private Integer idTipoPersona;

    // NUEVO: Conjunto de sedes asociadas a la persona
    private Set<SedeDto> sedes;

    private Boolean estado;

    private LocalDateTime fechaCreacion;
}