package com.auth.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TIPO_PERSONA")
public class TipoPersona {

    @Id
    @NotNull(message = "El idTipoPersona es obligatorio")
    @Column(name = "idtipopersona")
    private Integer idTipoPersona;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false)
    private Boolean estado;

    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @JsonIgnore
    @OneToMany(mappedBy = "tipoPersona")
    private List<Persona> personas;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}