package com.hotel.inventario.service.repository;

import com.hotel.inventario.service.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    List<Carrito> findByIdRecepcion(Integer idRecepcion);
    Optional<Carrito> findByIdRecepcionAndProducto_IdProducto(Integer idRecepcion, Integer idProducto);
    void deleteByIdRecepcionAndProducto_IdProducto(Integer idRecepcion, Integer idProducto);
    void deleteByIdRecepcion(Integer idRecepcion);
}