package com.hotel.hotel.service.service.impl;

import com.hotel.hotel.service.dto.EstadoHabitacionRequestDTO;
import com.hotel.hotel.service.dto.EstadoHabitacionResponseDTO;
import com.hotel.hotel.service.entity.EstadoHabitacion;
import com.hotel.hotel.service.exception.ResourceNotFoundException;
import com.hotel.hotel.service.repository.EstadoHabitacionRepository;
import com.hotel.hotel.service.service.EstadoHabitacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadoHabitacionServiceImpl implements EstadoHabitacionService {

    private final EstadoHabitacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<EstadoHabitacionResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoHabitacionResponseDTO buscarPorId(Integer id) {
        EstadoHabitacion entidad = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de habitación no encontrado con ID: " + id));
        return toDto(entidad);
    }

    @Override
    @Transactional
    public EstadoHabitacionResponseDTO guardar(EstadoHabitacionRequestDTO dto) {
        EstadoHabitacion entidad = EstadoHabitacion.builder()
                .codigo(dto.getCodigo())
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado())
                .build();
        EstadoHabitacion guardado = repository.save(entidad);
        return toDto(guardado);
    }

    @Override
    @Transactional
    public EstadoHabitacionResponseDTO actualizar(Integer id, EstadoHabitacionRequestDTO dto) {
        EstadoHabitacion entidad = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de habitación no encontrado con ID: " + id));

        entidad.setCodigo(dto.getCodigo());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setEstado(dto.getEstado());

        EstadoHabitacion actualizado = repository.save(entidad);
        return toDto(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        EstadoHabitacion entidad = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de habitación no encontrado con ID: " + id));
        repository.delete(entidad);
    }

    private EstadoHabitacionResponseDTO toDto(EstadoHabitacion entidad) {
        return EstadoHabitacionResponseDTO.builder()
                .idEstadoHabitacion(entidad.getIdEstadoHabitacion())
                .codigo(entidad.getCodigo())
                .descripcion(entidad.getDescripcion())
                .estado(entidad.getEstado())
                .build();
    }
}