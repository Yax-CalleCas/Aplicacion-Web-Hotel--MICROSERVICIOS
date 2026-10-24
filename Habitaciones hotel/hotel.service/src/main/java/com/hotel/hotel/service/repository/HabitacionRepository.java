package com.hotel.hotel.service.repository;

import com.hotel.hotel.service.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    List<Habitacion> findByIdHotel(Integer idHotel);

    Optional<Habitacion> findByIdHabitacionAndIdHotel(Integer idHabitacion, Integer idHotel);

    Optional<Habitacion> findByNumeroAndIdHotel(String numero, Integer idHotel);

    boolean existsByNumeroAndIdHotel(String numero, Integer idHotel);

    @Query("SELECT h FROM Habitacion h " +
            "LEFT JOIN FETCH h.estadoHabitacion " +
            "LEFT JOIN FETCH h.piso " +
            "LEFT JOIN FETCH h.categoria " +
            "LEFT JOIN FETCH h.imagenes " +
            "WHERE h.idHabitacion = :id")
    Optional<Habitacion> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT h FROM Habitacion h " +
            "LEFT JOIN FETCH h.estadoHabitacion " +
            "LEFT JOIN FETCH h.piso " +
            "LEFT JOIN FETCH h.categoria " +
            "LEFT JOIN FETCH h.imagenes " +
            "WHERE h.idHotel = :idHotel")
    List<Habitacion> findAllWithDetailsByHotel(@Param("idHotel") Integer idHotel);

    @Query("SELECT h FROM Habitacion h " +
            "LEFT JOIN FETCH h.estadoHabitacion " +
            "LEFT JOIN FETCH h.piso " +
            "LEFT JOIN FETCH h.categoria " +
            "LEFT JOIN FETCH h.imagenes")
    List<Habitacion> findAllWithDetails();
}