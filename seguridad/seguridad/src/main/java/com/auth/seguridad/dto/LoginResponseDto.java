package com.auth.seguridad.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private Integer idPersona;
    private String nombre;
    private String apellido;
    private String correo;
    private String tipoPersona;
    private Set<SedeDto> sedes;
    private String token;
}