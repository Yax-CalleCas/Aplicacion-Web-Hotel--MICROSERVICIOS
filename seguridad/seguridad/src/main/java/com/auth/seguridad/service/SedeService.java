package com.auth.seguridad.service;


import com.auth.seguridad.dto.SedeDto;
import com.auth.seguridad.entity.Sede;
import com.auth.seguridad.exception.BusinessException;
import com.auth.seguridad.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;

    public List<SedeDto> listarTodas() {
        return sedeRepository.findAll().stream().map(s ->
                SedeDto.builder()
                        .idSede(s.getIdSede())
                        .codigo(s.getCodigo())
                        .nombre(s.getNombre())
                        .estado(s.getEstado())
                        .fechaCreacion(s.getFechaCreacion())
                        .build()
        ).collect(Collectors.toList());
    }

    public SedeDto buscarPorId(Integer id) {
        Sede s = sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con ID: " + id));

        return SedeDto.builder()
                .idSede(s.getIdSede())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .estado(s.getEstado())
                .fechaCreacion(s.getFechaCreacion())
                .build();
    }
}