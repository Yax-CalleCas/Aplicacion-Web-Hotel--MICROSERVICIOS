package com.recepcion.hotel.service.impl;

import com.recepcion.hotel.dto.HabitacionEstadoMensajeDTO;
import com.recepcion.hotel.dto.RecepcionRequestDTO;
import com.recepcion.hotel.dto.RecepcionResponseDTO;
import com.recepcion.hotel.dto.SalidaRequest;
import com.recepcion.hotel.entity.Recepcion;
import com.recepcion.hotel.repository.RecepcionRepository;
import com.recepcion.hotel.service.RecepcionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecepcionServiceImpl implements RecepcionService {

    private static final int HABITACION_DISPONIBLE = 1;
    private static final int HABITACION_OCUPADA = 2;
    private static final int HABITACION_LIMPIEZA = 3;

    private static final BigDecimal CERO = new BigDecimal("0.00");

    private final RecepcionRepository recepcionRepository;
    private final StreamBridge streamBridge; // Reemplaza RestTemplate por mensajería asíncrona ultrarrápida con RabbitMQ

    // ---------------------------------------------------------
    // CONSULTAS
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionResponseDTO> listarTodas() {
        return recepcionRepository.findAll()
                .stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionResponseDTO> listarPorEstado(Boolean estado) {
        return recepcionRepository.findByEstado(estado)
                .stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecepcionResponseDTO buscarPorId(Integer id) {
        return mapearAResponseDTO(buscarRecepcion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RecepcionResponseDTO buscarActivaPorHabitacion(Integer idHabitacion) {
        validarId(idHabitacion, "El ID de la habitación es obligatorio");

        Recepcion recepcion = recepcionRepository
                .findTopByIdHabitacionAndEstadoOrderByFechaEntradaDesc(
                        idHabitacion, true)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe una recepción activa para la habitación ID: "
                                + idHabitacion));

        return mapearAResponseDTO(recepcion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionResponseDTO> listarPorCliente(Integer idCliente) {
        validarId(idCliente, "El ID del cliente es obligatorio");

        return recepcionRepository.findByIdCliente(idCliente)
                .stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // CHECK-IN
    // ---------------------------------------------------------

    @Override
    @Transactional
    public RecepcionResponseDTO registrarCheckIn(
            RecepcionRequestDTO request) {

        validarRequest(request);
        validarDatosReserva(request);

        Integer idHabitacion = request.getIdHabitacion();

        if (existeRecepcionActiva(idHabitacion)) {
            throw new IllegalStateException(
                    "La habitación ya tiene una recepción activa.");
        }

        BigDecimal precioInicial = dinero(request.getPrecioInicial());
        BigDecimal adelanto = dinero(
                request.getAdelanto() != null
                        ? request.getAdelanto()
                        : CERO);

        if (adelanto.compareTo(precioInicial) > 0) {
            throw new IllegalArgumentException(
                    "El adelanto no puede superar el precio de la reserva.");
        }

        Recepcion recepcion = Recepcion.builder()
                .idCliente(request.getIdCliente())
                .idHabitacion(idHabitacion)
                .fechaEntrada(request.getFechaEntrada())
                .fechaSalida(request.getFechaSalida())
                .precioInicial(precioInicial)
                .adelanto(adelanto)
                .precioRestante(precioInicial.subtract(adelanto))
                .totalPagado(adelanto)
                .costoPenalidad(CERO)
                .observacion(request.getObservacion())
                .estado(true)
                .build();

        Recepcion guardada = recepcionRepository.save(recepcion);

        // Notificación instantánea por RabbitMQ
        actualizarEstadoHabitacionPorId(idHabitacion, HABITACION_OCUPADA);

        return mapearAResponseDTO(guardada);
    }

    // CHECK-OUT (Con cálculo proporcional por salida anticipada)

    @Override
    @Transactional
    public RecepcionResponseDTO registrarCheckOut(
            Integer idRecepcion,
            SalidaRequest request) {

        validarId(idRecepcion, "El ID de la recepción es obligatorio");

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del check-out son obligatorios.");
        }

        if (request.getIdRecepcion() != null
                && !Objects.equals(
                request.getIdRecepcion(), idRecepcion)) {
            throw new IllegalArgumentException(
                    "El ID de la recepción no coincide con la solicitud.");
        }

        Recepcion recepcion = buscarRecepcion(idRecepcion);

        if (!Boolean.TRUE.equals(recepcion.getEstado())) {
            throw new IllegalStateException(
                    "Esta recepción ya tiene registrado el check-out.");
        }

        BigDecimal penalidad = dinero(
                request.getCostoPenalidad() != null
                        ? request.getCostoPenalidad()
                        : CERO);

        if (penalidad.signum() < 0) {
            throw new IllegalArgumentException(
                    "La penalidad no puede ser negativa.");
        }

        LocalDate fechaEntrada = recepcion.getFechaEntrada();
        LocalDate fechaSalidaReservada = recepcion.getFechaSalida();

        if (fechaEntrada == null
                || fechaSalidaReservada == null
                || !fechaSalidaReservada.isAfter(fechaEntrada)) {
            throw new IllegalStateException(
                    "La recepción tiene fechas de estancia inválidas.");
        }

        LocalDate fechaCheckOut = LocalDate.now();

        long nochesReservadas = ChronoUnit.DAYS.between(
                fechaEntrada, fechaSalidaReservada);

        long nochesUtilizadas = ChronoUnit.DAYS.between(
                fechaEntrada, fechaCheckOut);

        nochesUtilizadas = Math.max(1L, nochesUtilizadas);
        nochesUtilizadas = Math.min(
                nochesUtilizadas, nochesReservadas);

        BigDecimal precioInicial = dinero(recepcion.getPrecioInicial());

        if (precioInicial.signum() <= 0) {
            throw new IllegalStateException(
                    "El precio de la reserva debe ser mayor que cero.");
        }

        BigDecimal precioAlojamiento;

        if (nochesUtilizadas == nochesReservadas) {
            precioAlojamiento = precioInicial;
        } else {
            precioAlojamiento = precioInicial
                    .multiply(BigDecimal.valueOf(nochesUtilizadas))
                    .divide(
                            BigDecimal.valueOf(nochesReservadas),
                            2,
                            RoundingMode.HALF_UP);
        }

        BigDecimal totalFinal = precioAlojamiento.add(penalidad);

        BigDecimal totalPagado = dinero(
                recepcion.getTotalPagado());

        if (totalPagado.signum() < 0) {
            throw new IllegalStateException(
                    "La recepción tiene un total pagado inválido.");
        }

        BigDecimal precioRestante = totalFinal
                .subtract(totalPagado)
                .max(CERO);

        recepcion.setCostoPenalidad(penalidad);
        recepcion.setPrecioInicial(precioAlojamiento);
        recepcion.setPrecioRestante(precioRestante);
        recepcion.setTotalPagado(totalFinal);
        recepcion.setFechaSalidaConfirmacion(
                LocalDateTime.now());
        recepcion.setEstado(false);

        String detalle = "Check-out: "
                + fechaCheckOut
                + "; noches facturadas: "
                + nochesUtilizadas
                + "; alojamiento: "
                + precioAlojamiento
                + "; penalidad: "
                + penalidad
                + "; total: "
                + totalFinal;

        if (request.getObservacion() != null
                && !request.getObservacion().isBlank()) {
            detalle += "; " + request.getObservacion().trim();
        }

        String observacionActual = recepcion.getObservacion();

        if (observacionActual == null
                || observacionActual.isBlank()) {
            recepcion.setObservacion(limitarObservacion(detalle));
        } else {
            recepcion.setObservacion(
                    limitarObservacion(
                            observacionActual + " | " + detalle));
        }

        Recepcion actualizada = recepcionRepository.save(recepcion);

        // Notificación instantánea por RabbitMQ enviando la habitación a limpieza
        actualizarEstadoHabitacionPorId(
                recepcion.getIdHabitacion(),
                HABITACION_LIMPIEZA);

        log.info(
                "Check-out registrado. Recepción={}, habitación={}, "
                        + "noches={}, total={}, pagado={}, pendiente={}",
                idRecepcion,
                recepcion.getIdHabitacion(),
                nochesUtilizadas,
                totalFinal,
                totalPagado,
                precioRestante);

        return mapearAResponseDTO(actualizada);
    }

    // ---------------------------------------------------------
    // ACTUALIZACIÓN
    // ---------------------------------------------------------

    @Override
    @Transactional
    public RecepcionResponseDTO actualizar(
            Integer id,
            RecepcionRequestDTO dto) {

        validarId(id, "El ID de la recepción es obligatorio");
        validarRequest(dto);
        validarDatosReserva(dto);

        Recepcion recepcion = buscarRecepcion(id);

        if (!Boolean.TRUE.equals(recepcion.getEstado())) {
            throw new IllegalStateException(
                    "No se puede modificar una recepción finalizada. "
                            + "El historial debe conservarse.");
        }

        Integer habitacionAnterior = recepcion.getIdHabitacion();
        Integer habitacionNueva = dto.getIdHabitacion();

        boolean cambioHabitacion = !Objects.equals(
                habitacionAnterior, habitacionNueva);

        if (cambioHabitacion) {
            throw new IllegalStateException(
                    "No se permite cambiar la habitación de una "
                            + "recepción activa. Registra la operación "
                            + "de traslado mediante un flujo específico.");
        }

        BigDecimal nuevoPrecio = dinero(dto.getPrecioInicial());

        if (nuevoPrecio.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El precio inicial debe ser mayor que cero.");
        }

        BigDecimal nuevoAdelanto = dinero(
                dto.getAdelanto() != null
                        ? dto.getAdelanto()
                        : recepcion.getAdelanto());

        if (nuevoAdelanto.signum() < 0
                || nuevoAdelanto.compareTo(nuevoPrecio) > 0) {
            throw new IllegalArgumentException(
                    "El adelanto debe estar entre cero y el precio "
                            + "de la reserva.");
        }

        BigDecimal totalPagado = dinero(
                recepcion.getTotalPagado());

        if (nuevoAdelanto.compareTo(
                dinero(recepcion.getAdelanto())) != 0
                && totalPagado.compareTo(
                dinero(recepcion.getAdelanto())) != 0) {
            throw new IllegalStateException(
                    "No se puede modificar el adelanto porque el "
                            + "total pagado ya contiene otros importes.");
        }

        recepcion.setIdCliente(dto.getIdCliente());
        recepcion.setFechaEntrada(dto.getFechaEntrada());
        recepcion.setFechaSalida(dto.getFechaSalida());
        recepcion.setPrecioInicial(nuevoPrecio);
        recepcion.setAdelanto(nuevoAdelanto);
        recepcion.setTotalPagado(nuevoAdelanto);
        recepcion.setPrecioRestante(
                nuevoPrecio.subtract(nuevoAdelanto));
        recepcion.setObservacion(dto.getObservacion());

        return mapearAResponseDTO(
                recepcionRepository.save(recepcion));
    }

    // ---------------------------------------------------------
    // ELIMINACIÓN
    // ---------------------------------------------------------

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Recepcion recepcion = buscarRecepcion(id);

        if (Boolean.TRUE.equals(recepcion.getEstado())) {
            throw new IllegalStateException(
                    "No se puede eliminar una recepción activa. "
                            + "Registra primero su check-out.");
        }

        throw new IllegalStateException(
                "No se permite eliminar una recepción del historial. "
                        + "Implementa una anulación auditada si "
                        + "necesitas corregir un registro.");
    }

    // ---------------------------------------------------------
    // VALIDACIONES Y AUXILIARES
    // ---------------------------------------------------------

    private Recepcion buscarRecepcion(Integer id) {
        validarId(id, "El ID de la recepción es obligatorio");

        return recepcionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recepción no encontrada con ID: " + id));
    }

    private void validarId(Integer id, String mensaje) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarRequest(RecepcionRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la recepción son obligatorios.");
        }

        validarId(
                request.getIdCliente(),
                "El ID del cliente debe ser válido.");

        validarId(
                request.getIdHabitacion(),
                "El ID de la habitación debe ser válido.");

        if (request.getFechaEntrada() == null
                || request.getFechaSalida() == null) {
            throw new IllegalArgumentException(
                    "Las fechas de entrada y salida son obligatorias.");
        }

        if (!request.getFechaSalida().isAfter(
                request.getFechaEntrada())) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior "
                            + "a la fecha de entrada.");
        }

        if (request.getPrecioInicial() == null
                || request.getPrecioInicial().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El precio inicial debe ser mayor que cero.");
        }

        if (request.getAdelanto() != null
                && request.getAdelanto().signum() < 0) {
            throw new IllegalArgumentException(
                    "El adelanto no puede ser negativo.");
        }

        if (request.getObservacion() != null
                && request.getObservacion().length() > 500) {
            throw new IllegalArgumentException(
                    "La observación no puede superar los 500 caracteres.");
        }
    }

    private void validarDatosReserva(RecepcionRequestDTO request) {
        if (request.getFechaEntrada() == null
                || request.getFechaSalida() == null) {
            throw new IllegalArgumentException(
                    "Las fechas son obligatorias.");
        }

        if (!request.getFechaSalida().isAfter(
                request.getFechaEntrada())) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior "
                            + "a la fecha de entrada.");
        }
    }

    private boolean existeRecepcionActiva(Integer idHabitacion) {
        return recepcionRepository
                .findTopByIdHabitacionAndEstadoOrderByFechaEntradaDesc(
                        idHabitacion, true)
                .isPresent();
    }

    private BigDecimal dinero(BigDecimal valor) {
        if (valor == null) {
            return CERO;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String limitarObservacion(String observacion) {
        if (observacion == null) {
            return null;
        }
        if (observacion.length() <= 500) {
            return observacion;
        }
        return observacion.substring(0, 500);
    }

    /**
     * Envía el evento de cambio de estado a través de RabbitMQ (Spring Cloud Stream / StreamBridge).
     * Ya no bloquea la aplicación con llamadas HTTP síncronas.
     */
    private void actualizarEstadoHabitacionPorId(
            Integer idHabitacion,
            Integer idEstadoHabitacion) {

        try {
            HabitacionEstadoMensajeDTO mensaje = new HabitacionEstadoMensajeDTO(idHabitacion, idEstadoHabitacion);

            // Envía el mensaje al binding configurado en el application.yml (ej: habitacionEstadoOut-out-0)
            boolean enviado = streamBridge.send("habitacionEstadoOut-out-0", mensaje);

            if (enviado) {
                log.info("Evento publicado por RabbitMQ con éxito: habitación={}, estado={}",
                        idHabitacion, idEstadoHabitacion);
            } else {
                log.warn("El binder de RabbitMQ no pudo despachar el evento para la habitación={}", idHabitacion);
            }

        } catch (Exception e) {
            log.error("Error crítico al enviar evento por RabbitMQ para la habitación {}: {}",
                    idHabitacion, e.getMessage(), e);
            throw new IllegalStateException(
                    "No se pudo sincronizar el estado de la habitación mediante eventos de mensajería.", e);
        }
    }

    private RecepcionResponseDTO mapearAResponseDTO(
            Recepcion recepcion) {

        return RecepcionResponseDTO.builder()
                .idRecepcion(recepcion.getIdRecepcion())
                .idCliente(recepcion.getIdCliente())
                .idHabitacion(recepcion.getIdHabitacion())
                .fechaEntrada(recepcion.getFechaEntrada())
                .fechaSalida(recepcion.getFechaSalida())
                .fechaSalidaConfirmacion(
                        recepcion.getFechaSalidaConfirmacion())
                .precioInicial(recepcion.getPrecioInicial())
                .adelanto(recepcion.getAdelanto())
                .precioRestante(recepcion.getPrecioRestante())
                .totalPagado(recepcion.getTotalPagado())
                .costoPenalidad(recepcion.getCostoPenalidad())
                .observacion(recepcion.getObservacion())
                .estado(recepcion.getEstado())
                .fechaCreacion(recepcion.getFechaCreacion())
                .fechaActualizacion(recepcion.getFechaActualizacion())
                .build();
    }
}