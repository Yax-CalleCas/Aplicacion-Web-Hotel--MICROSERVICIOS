package com.hotel.hotel.service.service;

import com.hotel.hotel.service.dto.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HabitacionService {

    List<HabitacionResponseDTO> listarTodos();

    List<HabitacionResponseDTO> listarPorHotel(Integer idHotel);

    HabitacionResponseDTO buscarPorId(Integer id);

    HabitacionResponseDTO guardar(HabitacionRequestDTO dto);

    HabitacionResponseDTO actualizar(Integer id, HabitacionRequestDTO dto);

    void eliminar(Integer id);

    @Transactional
    HabitacionResponseDTO actualizarEstado(Integer idHabitacion, Integer idEstadoHabitacion);

    void completarMantenimiento(Integer id, String blockId, Long ifMatchVersion);

    void reactivar(Integer id, String blockId, Long ifMatchVersion);

    ImagenHabitacionDTO completarLimpieza(Integer id, String cleaningCycleId, Long ifMatchVersion, String idempotencyKey);
}