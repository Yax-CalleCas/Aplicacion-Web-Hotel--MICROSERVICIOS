package com.recepcion.hotel.repository;

import com.recepcion.hotel.entity.Recepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionRepository extends JpaRepository<Recepcion, Integer> {

    List<Recepcion> findByEstado(Boolean estado);

    List<Recepcion> findByIdHabitacionAndEstado(Integer idHabitacion, Boolean estado);

    List<Recepcion> findByIdCliente(Integer idCliente);

    Optional<Recepcion> findTopByIdHabitacionAndEstadoOrderByFechaEntradaDesc(Integer idHabitacion, Boolean estado);
}