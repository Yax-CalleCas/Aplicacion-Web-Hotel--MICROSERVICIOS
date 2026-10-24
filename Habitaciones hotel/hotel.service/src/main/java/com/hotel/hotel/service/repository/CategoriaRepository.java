package com.hotel.hotel.service.repository;

import com.hotel.hotel.service.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByDescripcion(String descripcion);

    boolean existsByDescripcion(String descripcion);
}