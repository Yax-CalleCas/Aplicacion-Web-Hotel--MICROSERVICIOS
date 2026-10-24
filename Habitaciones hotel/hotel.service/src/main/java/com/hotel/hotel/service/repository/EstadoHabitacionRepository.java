package com.hotel.hotel.service.repository;

import com.hotel.hotel.service.entity.EstadoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoHabitacionRepository extends JpaRepository<EstadoHabitacion, Integer> {

    boolean existsByCodigo(String codigo);

    Optional<EstadoHabitacion> findByCodigo(String codigo);
}