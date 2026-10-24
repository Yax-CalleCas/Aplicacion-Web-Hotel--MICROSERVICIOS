package com.hotel.hotel.service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "habitacion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hotel_numero_habitacion", columnNames = {"id_hotel", "numero"})
})
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idhabitacion")
    private Integer idHabitacion;

    @Column(name = "id_hotel", nullable = false)
    private Integer idHotel;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadohabitacion", nullable = false)
    private EstadoHabitacion estadoHabitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpiso", nullable = false)
    private Piso piso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoria", nullable = false)
    private Categoria categoria;

    @Builder.Default
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ImagenHabitacion> imagenes = new ArrayList<>();

    @Builder.Default
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
        if (this.version == null) {
            this.version = 1L;
        }
    }
}