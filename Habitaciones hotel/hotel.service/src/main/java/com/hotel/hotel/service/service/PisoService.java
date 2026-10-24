package com.hotel.hotel.service.service;

import com.hotel.hotel.service.dto.PisoRequestDTO;
import com.hotel.hotel.service.dto.PisoResponseDTO;
import java.util.List;

public interface PisoService {
    List<PisoResponseDTO> listarTodos();
    List<PisoResponseDTO> listarPorHotel(Integer idHotel);
    PisoResponseDTO buscarPorId(Integer id);
    PisoResponseDTO guardar(PisoRequestDTO dto);
    PisoResponseDTO actualizar(Integer id, PisoRequestDTO dto);
    void eliminar(Integer id);
}