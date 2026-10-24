package com.hotel.hotel.service.service;

import com.hotel.hotel.service.dto.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {
    List<CategoriaResponseDTO> listarTodos();
    CategoriaResponseDTO buscarPorId(Integer id);
    CategoriaResponseDTO guardar(CategoriaResponseDTO dto);
    CategoriaResponseDTO actualizar(Integer id, CategoriaResponseDTO dto);
    void eliminar(Integer id);
}