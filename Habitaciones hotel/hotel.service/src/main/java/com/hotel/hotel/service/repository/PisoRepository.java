package com.hotel.hotel.service.repository;

import com.hotel.hotel.service.entity.Piso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PisoRepository extends JpaRepository<Piso, Integer> {

    List<Piso> findByIdHotel(Integer idHotel);

    Optional<Piso> findByIdPisoAndIdHotel(Integer idPiso, Integer idHotel);

    Optional<Piso> findByDescripcionAndIdHotel(String descripcion, Integer idHotel);

    boolean existsByDescripcionAndIdHotel(String descripcion, Integer idHotel);

    @Query("SELECT p FROM Piso p LEFT JOIN FETCH p.habitaciones WHERE p.idPiso = :idPiso AND p.idHotel = :idHotel")
    Optional<Piso> findByIdWithHabitaciones(@Param("idPiso") Integer idPiso, @Param("idHotel") Integer idHotel);
}