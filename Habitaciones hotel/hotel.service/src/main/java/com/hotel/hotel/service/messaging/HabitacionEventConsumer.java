package com.hotel.hotel.service.messaging;

import com.hotel.hotel.service.dto.HabitacionEstadoMensajeDTO;
import com.hotel.hotel.service.service.HabitacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HabitacionEventConsumer {

    private final HabitacionService habitacionService;

    @RabbitListener(queues = "habitacion.estado.cambio.queue")
    public void consumirCambioEstadoHabitacion(HabitacionEstadoMensajeDTO mensaje) {
        log.info("Evento recibido de RabbitMQ (desde recepcion-service): Habitación ID {} -> Nuevo Estado ID {}",
                mensaje.getIdHabitacion(), mensaje.getIdEstadoHabitacion());
        try {
            // Actualiza la base de datos local y dispara el SSE hacia Angular
            habitacionService.actualizarEstado(mensaje.getIdHabitacion(), mensaje.getIdEstadoHabitacion());
            log.info("Habitación ID {} actualizada y notificada a Angular correctamente vía SSE.", mensaje.getIdHabitacion());
        } catch (Exception e) {
            log.error("Error al procesar el cambio de estado de la habitación por RabbitMQ: {}", e.getMessage(), e);
        }
    }
}