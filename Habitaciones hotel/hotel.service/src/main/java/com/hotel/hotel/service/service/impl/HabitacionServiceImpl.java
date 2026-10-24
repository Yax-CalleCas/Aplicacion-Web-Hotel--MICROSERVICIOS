package com.hotel.hotel.service.service.impl;

import com.hotel.hotel.service.dto.*;
import com.hotel.hotel.service.entity.*;
import com.hotel.hotel.service.messaging.HabitacionEventProducer;
import com.hotel.hotel.service.repository.*;
import com.hotel.hotel.service.service.HabitacionService;
import com.hotel.hotel.service.service.SseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final EstadoHabitacionRepository estadoHabitacionRepository;
    private final PisoRepository pisoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HabitacionEventProducer eventProducer;
    private final SseService sseService; // Notifica a Angular en tiempo real

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponseDTO> listarTodos() {
        return habitacionRepository.findAllWithDetails().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponseDTO> listarPorHotel(Integer idHotel) {
        return habitacionRepository.findAllWithDetailsByHotel(idHotel).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponseDTO buscarPorId(Integer id) {
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));
        return mapToResponseDTO(habitacion);
    }

    @Override
    @Transactional
    public HabitacionResponseDTO guardar(HabitacionRequestDTO dto) {
        if (habitacionRepository.existsByNumeroAndIdHotel(dto.getNumero(), dto.getIdHotel())) {
            throw new IllegalArgumentException("Ya existe la habitación número " + dto.getNumero() + " en esta sede.");
        }

        EstadoHabitacion estadoHabitacion = estadoHabitacionRepository.findById(dto.getIdEstadoHabitacion())
                .orElseThrow(() -> new EntityNotFoundException("Estado de habitación no encontrado con ID: " + dto.getIdEstadoHabitacion()));

        Piso piso = pisoRepository.findByIdPisoAndIdHotel(dto.getIdPiso(), dto.getIdHotel())
                .orElseThrow(() -> new EntityNotFoundException("Piso no encontrado o no pertenece a la sede especificada."));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        Habitacion habitacion = Habitacion.builder()
                .idHotel(dto.getIdHotel())
                .numero(dto.getNumero())
                .detalle(dto.getDetalle())
                .precio(dto.getPrecio())
                .estadoHabitacion(estadoHabitacion)
                .piso(piso)
                .categoria(categoria)
                .estado(true)
                .build();

        if (dto.getUrlsImagenes() != null && !dto.getUrlsImagenes().isEmpty()) {
            List<ImagenHabitacion> imagenes = dto.getUrlsImagenes().stream()
                    .map(url -> ImagenHabitacion.builder()
                            .urlImagen(url)
                            .habitacion(habitacion)
                            .build())
                    .collect(Collectors.toList());
            habitacion.setImagenes(imagenes);
        }

        Habitacion saved = habitacionRepository.save(habitacion);

        publicarEvento("HABITACION_CREADA", saved, saved.getEstadoHabitacion().getCodigo());

        // --- NOTIFICAR A ANGULAR EN TIEMPO REAL VÍA SSE ---
        HabitacionResponseDTO respuesta = mapToResponseDTO(saved);
        sseService.notificarCambioHabitacion(respuesta);

        return respuesta;
    }

    @Override
    @Transactional
    public HabitacionResponseDTO actualizar(Integer id, HabitacionRequestDTO dto) {
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));

        if ((!habitacion.getNumero().equals(dto.getNumero()) || !habitacion.getIdHotel().equals(dto.getIdHotel())) &&
                habitacionRepository.existsByNumeroAndIdHotel(dto.getNumero(), dto.getIdHotel())) {
            throw new IllegalArgumentException("Ya existe otra habitación número " + dto.getNumero() + " en esta sede.");
        }

        EstadoHabitacion estadoHabitacion = estadoHabitacionRepository.findById(dto.getIdEstadoHabitacion())
                .orElseThrow(() -> new EntityNotFoundException("Estado de habitación no encontrado con ID: " + dto.getIdEstadoHabitacion()));

        Piso piso = pisoRepository.findByIdPisoAndIdHotel(dto.getIdPiso(), dto.getIdHotel())
                .orElseThrow(() -> new EntityNotFoundException("Piso no encontrado o no pertenece a la sede especificada."));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        habitacion.setIdHotel(dto.getIdHotel());
        habitacion.setNumero(dto.getNumero());
        habitacion.setDetalle(dto.getDetalle());
        habitacion.setPrecio(dto.getPrecio());
        habitacion.setEstadoHabitacion(estadoHabitacion);
        habitacion.setPiso(piso);
        habitacion.setCategoria(categoria);

        if (habitacion.getImagenes() != null) {
            habitacion.getImagenes().clear();
        }
        if (dto.getUrlsImagenes() != null && !dto.getUrlsImagenes().isEmpty()) {
            List<ImagenHabitacion> nuevasImagenes = dto.getUrlsImagenes().stream()
                    .map(url -> ImagenHabitacion.builder()
                            .urlImagen(url)
                            .habitacion(habitacion)
                            .build())
                    .collect(Collectors.toList());
            if (habitacion.getImagenes() == null) {
                habitacion.setImagenes(nuevasImagenes);
            } else {
                habitacion.getImagenes().addAll(nuevasImagenes);
            }
        }

        Habitacion updated = habitacionRepository.save(habitacion);

        publicarEvento("HABITACION_ACTUALIZADA", updated, estadoHabitacion.getCodigo());

        // --- NOTIFICAR A ANGULAR EN TIEMPO REAL VÍA SSE ---
        HabitacionResponseDTO respuesta = mapToResponseDTO(updated);
        sseService.notificarCambioHabitacion(respuesta);

        return respuesta;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));
        habitacionRepository.delete(habitacion);

        publicarEvento("HABITACION_ELIMINADA", habitacion, "ELIMINADA");
    }

    @Override
    @Transactional
    public HabitacionResponseDTO actualizarEstado(Integer idHabitacion, Integer idEstadoHabitacion) {
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(idHabitacion)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + idHabitacion));

        EstadoHabitacion estadoHabitacion = estadoHabitacionRepository.findById(idEstadoHabitacion)
                .orElseThrow(() -> new EntityNotFoundException("Estado de habitación no encontrado con ID: " + idEstadoHabitacion));

        habitacion.setEstadoHabitacion(estadoHabitacion);
        Habitacion updated = habitacionRepository.save(habitacion);

        // Publicar a RabbitMQ
        publicarEvento("HABITACION_ACTUALIZADA", updated, estadoHabitacion.getCodigo());

        // --- NOTIFICAR A ANGULAR EN TIEMPO REAL VÍA SSE ---
        HabitacionResponseDTO responseDTO = mapToResponseDTO(updated);
        sseService.notificarCambioHabitacion(responseDTO);

        return responseDTO;
    }

    @Override
    @Transactional
    public void completarMantenimiento(Integer id, String blockId, Long ifMatchVersion) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));

        EstadoHabitacion estadoDisponible = estadoHabitacionRepository.findByCodigo("DISPONIBLE")
                .orElseThrow(() -> new IllegalStateException("El estado operativo 'DISPONIBLE' no está configurado en el sistema."));

        habitacion.setEstadoHabitacion(estadoDisponible);
        Habitacion updated = habitacionRepository.save(habitacion);

        publicarEvento("HABITACION_ACTUALIZADA", updated, "DISPONIBLE");
        sseService.notificarCambioHabitacion(mapToResponseDTO(updated));
    }

    @Override
    @Transactional
    public void reactivar(Integer id, String blockId, Long ifMatchVersion) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));
        habitacion.setEstado(true);
        Habitacion updated = habitacionRepository.save(habitacion);

        publicarEvento("HABITACION_ACTUALIZADA", updated, updated.getEstadoHabitacion().getCodigo());
        sseService.notificarCambioHabitacion(mapToResponseDTO(updated));
    }

    @Override
    @Transactional
    public ImagenHabitacionDTO completarLimpieza(Integer id, String cleaningCycleId, Long ifMatchVersion, String idempotencyKey) {
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Habitación no encontrada con ID: " + id));

        EstadoHabitacion estadoDisponible = estadoHabitacionRepository.findByCodigo("DISPONIBLE")
                .orElseThrow(() -> new IllegalStateException("El estado 'DISPONIBLE' no está configurado."));
        habitacion.setEstadoHabitacion(estadoDisponible);
        Habitacion updated = habitacionRepository.save(habitacion);

        publicarEvento("HABITACION_ACTUALIZADA", updated, "DISPONIBLE");
        sseService.notificarCambioHabitacion(mapToResponseDTO(updated));

        if (habitacion.getImagenes() != null && !habitacion.getImagenes().isEmpty()) {
            ImagenHabitacion img = habitacion.getImagenes().get(0);
            return ImagenHabitacionDTO.builder()
                    .idImagen(img.getIdImagen())
                    .urlImagen(img.getUrlImagen())
                    .build();
        }
        return null;
    }

    /**
     * Publica el evento en RabbitMQ. Si el broker falla, solo se registra el error:
     * la operación en base de datos no se revierte.
     */
    private void publicarEvento(String tipoEvento, Habitacion h, String codigoEstado) {
        try {
            HabitacionEventDTO evento = HabitacionEventDTO.builder()
                    .eventType(tipoEvento)
                    .idHabitacion(h.getIdHabitacion())
                    .idHotel(h.getIdHotel())
                    .nuevoEstado(codigoEstado)
                    .timestamp(LocalDateTime.now())
                    .build();
            eventProducer.enviarEventoHabitacion(evento);
        } catch (Exception e) {
            log.error("No se pudo publicar el evento {} de la habitación {}: {}",
                    tipoEvento, h.getIdHabitacion(), e.getMessage());
        }
    }

    private HabitacionResponseDTO mapToResponseDTO(Habitacion h) {
        return HabitacionResponseDTO.builder()
                .idHabitacion(h.getIdHabitacion())
                .idHotel(h.getIdHotel())
                .numero(h.getNumero())
                .detalle(h.getDetalle())
                .precio(h.getPrecio())
                .estado(h.getEstado())
                .estadoHabitacion(h.getEstadoHabitacion() != null ? EstadoHabitacionResponseDTO.builder()
                        .idEstadoHabitacion(h.getEstadoHabitacion().getIdEstadoHabitacion())
                        .codigo(h.getEstadoHabitacion().getCodigo())
                        .descripcion(h.getEstadoHabitacion().getDescripcion())
                        .estado(h.getEstadoHabitacion().getEstado())
                        .build() : null)
                .piso(h.getPiso() != null ? PisoResponseDTO.builder()
                        .idPiso(h.getPiso().getIdPiso())
                        .idHotel(h.getPiso().getIdHotel())
                        .descripcion(h.getPiso().getDescripcion())
                        .estado(h.getPiso().getEstado())
                        .build() : null)
                .categoria(h.getCategoria() != null ? CategoriaResponseDTO.builder()
                        .idCategoria(h.getCategoria().getIdCategoria())
                        .descripcion(h.getCategoria().getDescripcion())
                        .estado(h.getCategoria().getEstado())
                        .build() : null)
                .imagenes(h.getImagenes() != null ? h.getImagenes().stream()
                        .map(img -> ImagenHabitacionDTO.builder()
                                .idImagen(img.getIdImagen())
                                .urlImagen(img.getUrlImagen())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}