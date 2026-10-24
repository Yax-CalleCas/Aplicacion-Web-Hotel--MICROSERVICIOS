package com.auth.seguridad.service.impl;

import com.auth.seguridad.dto.TipoPersonaDto;
import com.auth.seguridad.entity.TipoPersona;
import com.auth.seguridad.exception.BusinessException;
import com.auth.seguridad.exception.ResourceNotFoundException;
import com.auth.seguridad.repository.TipoPersonaRepository;
import com.auth.seguridad.service.TipoPersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoPersonaServiceImpl implements TipoPersonaService {

    private final TipoPersonaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoPersonaDto> listarTodos() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoPersonaDto buscarPorId(Integer id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    @Override
    @Transactional
    public TipoPersonaDto guardar(TipoPersonaDto dto) {
        if (dto.getIdTipoPersona() == null) {
            throw new BusinessException("Error: El idTipoPersona es obligatorio.");
        }
        if (repository.existsById(dto.getIdTipoPersona())) {
            throw new BusinessException("Error: El tipo de persona ya existe.");
        }
        return toDto(repository.save(toEntity(dto)));
    }

    @Override
    @Transactional
    public TipoPersonaDto actualizar(Integer id, TipoPersonaDto dto) {
        return repository.findById(id).map(e -> {
            e.setDescripcion(dto.getDescripcion());
            e.setEstado(dto.getEstado());
            return toDto(repository.save(e));
        }).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoPersona t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        t.setEstado(false);
        repository.save(t);
    }

    private TipoPersonaDto toDto(TipoPersona e) {
        return TipoPersonaDto.builder()
                .idTipoPersona(e.getIdTipoPersona())
                .descripcion(e.getDescripcion())
                .estado(e.getEstado())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }

    private TipoPersona toEntity(TipoPersonaDto d) {
        return TipoPersona.builder()
                .idTipoPersona(d.getIdTipoPersona())
                .descripcion(d.getDescripcion())
                .estado(d.getEstado())
                .fechaCreacion(d.getFechaCreacion())
                .build();
    }
}