package com.hotel.hotel.service.config;

import com.hotel.hotel.service.entity.Categoria;
import com.hotel.hotel.service.entity.EstadoHabitacion;
import com.hotel.hotel.service.entity.Habitacion;
import com.hotel.hotel.service.entity.Piso;
import com.hotel.hotel.service.repository.CategoriaRepository;
import com.hotel.hotel.service.repository.EstadoHabitacionRepository;
import com.hotel.hotel.service.repository.HabitacionRepository;
import com.hotel.hotel.service.repository.PisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EstadoHabitacionRepository estadoHabitacionRepository;
    private final PisoRepository pisoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HabitacionRepository habitacionRepository;

    @Override
    public void run(String... args) {
        // 1. Inicializar Estados de Habitación
        if (estadoHabitacionRepository.count() == 0) {
            estadoHabitacionRepository.save(EstadoHabitacion.builder().descripcion("Disponible").codigo("DISP").estado(true).version(1L).build());
            estadoHabitacionRepository.save(EstadoHabitacion.builder().descripcion("Ocupado").codigo("OCUP").estado(true).version(1L).build());
            estadoHabitacionRepository.save(EstadoHabitacion.builder().descripcion("Mantenimiento").codigo("MANT").estado(true).version(1L).build());
            System.out.println("--- ESTADOS DE HABITACIÓN CREADOS ---");
        }

        // 2. Inicializar Pisos (Asociados al idHotel = 1 para cumplir con el modelo multisede)
        if (pisoRepository.count() == 0) {
            pisoRepository.save(Piso.builder().idHotel(1).descripcion("Primero").estado(true).build());
            pisoRepository.save(Piso.builder().idHotel(1).descripcion("Segundo").estado(true).build());
            pisoRepository.save(Piso.builder().idHotel(1).descripcion("Tercero").estado(true).build());
            System.out.println("--- PISOS CREADOS ---");
        }

        // 3. Inicializar Categorías
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(Categoria.builder().descripcion("Individual").estado(true).build());
            categoriaRepository.save(Categoria.builder().descripcion("VIP").estado(true).build());
            categoriaRepository.save(Categoria.builder().descripcion("Matrimonial").estado(true).build());
            System.out.println("--- CATEGORÍAS CREADAS ---");
        }

        // 4. Inicializar 3 Habitaciones de Ejemplo
        if (habitacionRepository.count() == 0) {
            var disp = estadoHabitacionRepository.findAll().stream().filter(e -> e.getDescripcion() != null && e.getDescripcion().equals("Disponible")).findFirst().orElse(null);
            var ocup = estadoHabitacionRepository.findAll().stream().filter(e -> e.getDescripcion().equals("Ocupado")).findFirst().orElse(null);
            var mant = estadoHabitacionRepository.findAll().stream().filter(e -> e.getDescripcion().equals("Mantenimiento")).findFirst().orElse(null);

            var piso1 = pisoRepository.findAll().stream().filter(p -> p.getDescripcion().equals("Primero")).findFirst().orElse(null);
            var piso2 = pisoRepository.findAll().stream().filter(p -> p.getDescripcion().equals("Segundo")).findFirst().orElse(null);
            var piso3 = pisoRepository.findAll().stream().filter(p -> p.getDescripcion().equals("Tercero")).findFirst().orElse(null);

            var catInd = categoriaRepository.findAll().stream().filter(c -> c.getDescripcion().equals("Individual")).findFirst().orElse(null);
            var catVip = categoriaRepository.findAll().stream().filter(c -> c.getDescripcion().equals("VIP")).findFirst().orElse(null);
            var catMat = categoriaRepository.findAll().stream().filter(c -> c.getDescripcion().equals("Matrimonial")).findFirst().orElse(null);

            // Habitación 1: Individual en el Primer Piso (Disponible)
            if (piso1 != null && catInd != null && disp != null) {
                Habitacion h1 = new Habitacion();
                h1.setNumero("101");
                h1.setDetalle("Habitación individual estándar con vista a la calle");
                h1.setPrecio(new BigDecimal("80.00"));
                h1.setIdHotel(1);
                h1.setPiso(piso1);
                h1.setCategoria(catInd);
                h1.setEstadoHabitacion(disp);
                h1.setEstado(true);
                habitacionRepository.save(h1);
            }

            // Habitación 3: Matrimonial en el Tercer Piso (Mantenimiento)
            if (piso3 != null && catMat != null && mant != null) {
                Habitacion h3 = new Habitacion();
                h3.setNumero("303");
                h3.setDetalle("Habitación matrimonial amplia con cama King size");
                h3.setPrecio(new BigDecimal("150.00"));
                h3.setIdHotel(1);
                h3.setPiso(piso3);
                h3.setCategoria(catMat);
                h3.setEstadoHabitacion(mant);
                h3.setEstado(true);
                habitacionRepository.save(h3);
            }

            System.out.println("--- HABITACIONES DE EJEMPLO CREADAS EXITOSAMENTE ---");
        }
    }
}