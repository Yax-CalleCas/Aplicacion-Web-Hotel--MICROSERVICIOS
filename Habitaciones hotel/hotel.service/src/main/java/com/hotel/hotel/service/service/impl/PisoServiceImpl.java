package com.hotel.hotel.service.service.impl;

import com.hotel.hotel.service.dto.PisoRequestDTO;
import com.hotel.hotel.service.dto.PisoResponseDTO;
import com.hotel.hotel.service.entity.Piso;
import com.hotel.hotel.service.repository.PisoRepository;
import com.hotel.hotel.service.service.PisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisoServiceImpl implements PisoService {

    private final PisoRepository pisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PisoResponseDTO> listarTodos() {
        return pisoRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PisoResponseDTO> listarPorHotel(Integer idHotel) {
        return pisoRepository.findByIdHotel(idHotel).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PisoResponseDTO buscarPorId(Integer id) {
        Piso piso = pisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Piso no encontrado con ID: " + id));
        return mapToResponseDTO(piso);
    }

    @Override
    @Transactional
    public PisoResponseDTO guardar(PisoRequestDTO dto) {
        if (pisoRepository.existsByDescripcionAndIdHotel(dto.getDescripcion(), dto.getIdHotel())) {
            throw new RuntimeException("Ya existe un piso con la descripción '" + dto.getDescripcion() + "' en esta sede.");
        }

        Piso piso = Piso.builder()
                .idHotel(dto.getIdHotel())
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado() != null ? dto.getEstado() : true)
                .build();

        Piso saved = pisoRepository.save(piso);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public PisoResponseDTO actualizar(Integer id, PisoRequestDTO dto) {
        Piso piso = pisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Piso no encontrado con ID: " + id));

        if ((!piso.getDescripcion().equals(dto.getDescripcion()) || !piso.getIdHotel().equals(dto.getIdHotel())) &&
                pisoRepository.existsByDescripcionAndIdHotel(dto.getDescripcion(), dto.getIdHotel())) {
            throw new RuntimeException("Ya existe un piso con la descripción '" + dto.getDescripcion() + "' en esta sede.");
        }

        piso.setIdHotel(dto.getIdHotel());
        piso.setDescripcion(dto.getDescripcion());
        if (dto.getEstado() != null) {
            piso.setEstado(dto.getEstado());
        }

        Piso updated = pisoRepository.save(piso);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!pisoRepository.existsById(id)) {
            throw new RuntimeException("Piso no encontrado con ID: " + id);
        }
        pisoRepository.deleteById(id);
    }

    private PisoResponseDTO mapToResponseDTO(Piso piso) {
        return PisoResponseDTO.builder()
                .idPiso(piso.getIdPiso())
                .idHotel(piso.getIdHotel())
                .descripcion(piso.getDescripcion())
                .estado(piso.getEstado())
                .build();
    }
}