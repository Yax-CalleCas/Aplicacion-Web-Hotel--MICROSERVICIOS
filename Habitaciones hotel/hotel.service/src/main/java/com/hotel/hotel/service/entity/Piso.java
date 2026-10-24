package com.hotel.hotel.service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "piso", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hotel_descripcion_piso", columnNames = {"id_hotel", "descripcion"})
})
public class Piso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpiso")
    private Integer idPiso;

    @Column(name = "id_hotel", nullable = false)
    private Integer idHotel;

    @Column(name = "descripcion", nullable = false, length = 50)
    private String descripcion;

    @Builder.Default
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "piso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Habitacion> habitaciones = new ArrayList<>();

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