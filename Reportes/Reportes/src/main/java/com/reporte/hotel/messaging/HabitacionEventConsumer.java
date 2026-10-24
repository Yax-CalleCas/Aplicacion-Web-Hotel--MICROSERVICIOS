package com.reporte.hotel.messaging;

import com.reporte.hotel.config.RabbitMQConfig;
import com.reporte.hotel.dto.HabitacionEventDTO;
import com.reporte.hotel.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HabitacionEventConsumer {

    private final SseService sseService;

    @RabbitListener(queues = RabbitMQConfig.HABITACION_QUEUE)
    public void consumirEventoHabitacion(HabitacionEventDTO evento) {
        // Al recibir el evento de RabbitMQ, notificamos a todos los clientes conectados por SSE al instante
        sseService.enviarNotificacion(evento);
    }
}