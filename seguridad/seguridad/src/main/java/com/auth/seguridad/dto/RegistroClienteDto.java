package com.auth.seguridad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroClienteDto {

    @NotBlank
    private String tipoDocumento;

    @NotBlank
    private String documento;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
    private String clave;
}