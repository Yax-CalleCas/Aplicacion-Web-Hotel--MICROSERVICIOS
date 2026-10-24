package com.auth.seguridad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idPersona;
    private String tipoDocumento;
    private String documento;
    private String nombre;
    private String apellido;
    private String correo;
    private String fotoUrl;
    private Integer idTipoPersona;
    private String descripcionTipoPersona;
    private Boolean estado;
    private String eventType; // CREATED, UPDATED, DELETED
    private LocalDateTime timestamp;
}