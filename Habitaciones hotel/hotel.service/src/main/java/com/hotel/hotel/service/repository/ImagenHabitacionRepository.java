package com.hotel.hotel.service.repository;

import com.hotel.hotel.service.entity.ImagenHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenHabitacionRepository extends JpaRepository<ImagenHabitacion, Integer> {

    List<ImagenHabitacion> findByHabitacion_IdHabitacion(Integer idHabitacion);

    @Modifying
    @Query("DELETE FROM ImagenHabitacion i WHERE i.habitacion.idHabitacion = :idHabitacion")
    void deleteByHabitacion_IdHabitacion(@Param("idHabitacion") Integer idHabitacion);
}