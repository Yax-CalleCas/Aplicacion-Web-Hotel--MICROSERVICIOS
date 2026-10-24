package com.auth.seguridad.repository;

import com.auth.seguridad.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    @Query("SELECT DISTINCT p FROM Persona p JOIN FETCH p.tipoPersona LEFT JOIN FETCH p.sedes")
    List<Persona> findAllConTipo();

    @Query("SELECT p FROM Persona p JOIN FETCH p.tipoPersona LEFT JOIN FETCH p.sedes WHERE p.correo = :correo")
    Optional<Persona> findByCorreoWithTipo(@Param("correo") String correo);

    Optional<Persona> findByCorreo(String correo);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdPersonaNot(String documento, Integer idPersona);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdPersonaNot(String correo, Integer idPersona);
}