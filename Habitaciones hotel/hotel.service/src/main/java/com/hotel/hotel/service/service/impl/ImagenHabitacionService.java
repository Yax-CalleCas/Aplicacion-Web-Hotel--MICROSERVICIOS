package com.hotel.hotel.service.service.impl;

import com.hotel.hotel.service.entity.ImagenHabitacion;
import com.hotel.hotel.service.repository.ImagenHabitacionRepository;
import com.hotel.hotel.service.repository.HabitacionRepository;
import com.hotel.hotel.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImagenHabitacionService {

    private final ImagenHabitacionRepository repository;
    private final HabitacionRepository habitacionRepository;

    @Transactional
    public void agregarImagen(Integer habitacionId, String url) {
        var habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada con ID: " + habitacionId));

        var imagen = ImagenHabitacion.builder()
                .urlImagen(url)
                .habitacion(habitacion)
                .build();

        repository.save(imagen);
    }

    @Transactional
    public void eliminarImagen(Integer imagenId) {
        if (!repository.existsById(imagenId)) {
            throw new ResourceNotFoundException("Imagen no encontrada con ID: " + imagenId);
        }
        repository.deleteById(imagenId);
    }
}