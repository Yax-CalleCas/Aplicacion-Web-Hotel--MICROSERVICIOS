package com.recepcion.hotel.service;

import com.recepcion.hotel.dto.SalidaRequest;
import com.recepcion.hotel.dto.RecepcionRequestDTO;
import com.recepcion.hotel.dto.RecepcionResponseDTO;

import java.util.List;

public interface RecepcionService {
    List<RecepcionResponseDTO> listarTodas();
    List<RecepcionResponseDTO> listarPorEstado(Boolean estado);
    RecepcionResponseDTO buscarPorId(Integer id);
    RecepcionResponseDTO buscarActivaPorHabitacion(Integer idHabitacion);
    List<RecepcionResponseDTO> listarPorCliente(Integer idCliente);
    RecepcionResponseDTO registrarCheckIn(RecepcionRequestDTO request);
    RecepcionResponseDTO registrarCheckOut(Integer idRecepcion, SalidaRequest request);
    RecepcionResponseDTO actualizar(Integer id, RecepcionRequestDTO dto);
    void eliminar(Integer id);
}