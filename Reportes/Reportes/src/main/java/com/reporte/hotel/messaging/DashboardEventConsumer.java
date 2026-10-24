package com.reporte.hotel.messaging;

import com.reporte.hotel.config.RabbitMQConfig;
import com.reporte.hotel.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardEventConsumer {

    private final EstadisticasService estadisticasService;

    @RabbitListener(queues = {RabbitMQConfig.HABITACION_QUEUE, RabbitMQConfig.VENTA_QUEUE, RabbitMQConfig.RECEPCION_QUEUE})
    public void consumirEventosGlobales(Object mensaje) {
        log.info("Evento recibido en el servicio de reportes mediante RabbitMQ. Refrescando estadísticas...");
        try {
            // Refresca el snapshot en memoria de manera inmediata y empuja el SSE al frontend
            estadisticasService.refrescar();
        } catch (Exception e) {
            log.error("Error al refrescar estadísticas tras evento de RabbitMQ: {}", e.getMessage(), e);
        }
    }
}