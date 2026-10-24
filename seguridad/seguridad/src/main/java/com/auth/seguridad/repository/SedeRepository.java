package com.auth.seguridad.repository;

import com.auth.seguridad.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {

    @Query("SELECT s FROM Sede s WHERE s.idSede IN :ids AND s.estado = true")
    List<Sede> findAllByIdInAndEstadoTrue(@Param("ids") Collection<Integer> ids);
    Optional<Sede> findByCodigo(String codigo);
}