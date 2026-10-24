package com.hotel.inventario.service.Config;


import com.hotel.inventario.service.entity.Producto;
import com.hotel.inventario.service.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            // 1. Bebida Energizante
            productoRepository.save(Producto.builder()
                    .nombre("Bebida Energizante Red Bull 250ml")
                    .detalle("Lata de 250ml ideal para recargar energías.")
                    .precio(new BigDecimal("9.50"))
                    .cantidad(50)
                    .estado(true)
                    .imagenUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRU8soAEbldOaSq2jPyKH5ipHRcgPG_QuWxnDUJWCnKYJCXV3CkfNPJTBon&s=10")
                    .build());

            // 2. Agua Mineral
            productoRepository.save(Producto.builder()
                    .nombre("Agua Mineral San Mateo Sin Gas 600ml")
                    .detalle("Agua embotellada de manantial sin gas.")
                    .precio(new BigDecimal("4.00"))
                    .cantidad(100)
                    .estado(true)
                    .imagenUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_wyBCHUfJ_B1F4Z8oB9SoTAYAl8HVmn9hQxHMtIgvRA&s=10")
                    .build());

            // 3. Gaseosa Personal
            productoRepository.save(Producto.builder()
                    .nombre("Gaseosa Coca-Cola Personal 500ml")
                    .detalle("Bebida gaseosa sabor original en botella personal.")
                    .precio(new BigDecimal("5.00"))
                    .cantidad(80)
                    .estado(true)
                    .imagenUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcREFWtscITsnHVzcu2lQj4V6XG3nyi6H6e0bV551LKNUkdszRQJvkptzS4&s=10")
                    .build());

            // 4. Cerveza Artesanal / Nacional
            productoRepository.save(Producto.builder()
                    .nombre("Cerveza Cusqueña Trigo Botella 330ml")
                    .detalle("Cerveza premium de alta calidad, tipo trigo.")
                    .precio(new BigDecimal("8.50"))
                    .cantidad(60)
                    .estado(true)
                    .imagenUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTYcqXDjgXVCYHdtiNGTFw7U49fP9bB2QUl6mc7K9IlfVpum6P6f32My0jF&s=10")
                    .build());

            System.out.println("--- 4 PRODUCTOS DE EJEMPLO (BEBIDAS) CREADOS EXITOSAMENTE ---");
        }
    }
}