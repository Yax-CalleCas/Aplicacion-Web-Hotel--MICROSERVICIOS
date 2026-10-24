package com.hotel.inventario.service.repository;

import com.hotel.inventario.service.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByIdRecepcion(Integer idRecepcion);
}