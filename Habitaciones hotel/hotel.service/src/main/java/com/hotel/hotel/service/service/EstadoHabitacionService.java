package com.hotel.hotel.service.service;

import com.hotel.hotel.service.dto.EstadoHabitacionRequestDTO;
import com.hotel.hotel.service.dto.EstadoHabitacionResponseDTO;
import java.util.List;

public interface EstadoHabitacionService {
    List<EstadoHabitacionResponseDTO> listarTodos();
    EstadoHabitacionResponseDTO buscarPorId(Integer id);
    EstadoHabitacionResponseDTO guardar(EstadoHabitacionRequestDTO dto);
    EstadoHabitacionResponseDTO actualizar(Integer id, EstadoHabitacionRequestDTO dto);
    void eliminar(Integer id);
}