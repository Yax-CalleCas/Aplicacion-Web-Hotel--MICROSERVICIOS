package com.auth.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PERSONA")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpersona")
    private Integer idPersona;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "tipodocumento", nullable = false)
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 15, message = "El documento debe tener entre 8 y 15 caracteres")
    @Column(name = "documento", nullable = false, unique = true)
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Email(message = "El formato del correo es inválido")
    @NotBlank(message = "El correo es obligatorio")
    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "La clave es obligatoria")
    @Column(name = "clave", nullable = false)
    private String clave;

    @Column(name = "foto_url")
    private String fotoUrl;

    @NotNull(message = "El tipo de persona es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "idtipopersona", nullable = false)
    private TipoPersona tipoPersona;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "persona_sede",
            joinColumns = @JoinColumn(name = "idpersona"),
            inverseJoinColumns = @JoinColumn(name = "idsede")
    )
    @Builder.Default
    private Set<Sede> sedes = new HashSet<>();

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false)
    private Boolean estado;

    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
    }
}