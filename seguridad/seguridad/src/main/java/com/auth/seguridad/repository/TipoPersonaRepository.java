package com.auth.seguridad.repository;

import com.auth.seguridad.entity.TipoPersona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoPersonaRepository extends JpaRepository<TipoPersona, Integer> {

    Optional<TipoPersona> findByDescripcionIgnoreCase(String descripcion);
}