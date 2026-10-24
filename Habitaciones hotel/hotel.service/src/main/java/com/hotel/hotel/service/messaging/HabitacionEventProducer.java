package com.hotel.hotel.service.messaging;

import com.hotel.hotel.service.dto.HabitacionEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HabitacionEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE_NAME = "hotel.exchange";
    public static final String ROUTING_KEY = "habitacion.actualizada";

    public void enviarEventoHabitacion(HabitacionEventDTO evento) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, evento);
    }
}