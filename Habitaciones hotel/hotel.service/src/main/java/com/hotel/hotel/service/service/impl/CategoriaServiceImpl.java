package com.hotel.hotel.service.service.impl;

import com.hotel.hotel.service.dto.CategoriaResponseDTO;
import com.hotel.hotel.service.entity.Categoria;
import com.hotel.hotel.service.repository.CategoriaRepository;
import com.hotel.hotel.service.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodos() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return mapToResponseDTO(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO guardar(CategoriaResponseDTO dto) {
        if (categoriaRepository.existsByDescripcion(dto.getDescripcion())) {
            throw new RuntimeException("Ya existe una categoría con la descripción: " + dto.getDescripcion());
        }

        Categoria categoria = Categoria.builder()
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado() != null ? dto.getEstado() : true)
                .build();

        Categoria saved = categoriaRepository.save(categoria);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Integer id, CategoriaResponseDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        if (!categoria.getDescripcion().equals(dto.getDescripcion()) &&
                categoriaRepository.existsByDescripcion(dto.getDescripcion())) {
            throw new RuntimeException("Ya existe otra categoría con la descripción: " + dto.getDescripcion());
        }

        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getEstado() != null) {
            categoria.setEstado(dto.getEstado());
        }

        Categoria updated = categoriaRepository.save(categoria);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    private CategoriaResponseDTO mapToResponseDTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .descripcion(categoria.getDescripcion())
                .estado(categoria.getEstado())
                .build();
    }
}