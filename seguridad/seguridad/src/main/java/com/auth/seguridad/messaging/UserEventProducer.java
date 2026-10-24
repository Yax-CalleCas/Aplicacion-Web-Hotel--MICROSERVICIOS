package com.auth.seguridad.messaging;

import com.auth.seguridad.config.RabbitMQConfig;
import com.auth.seguridad.dto.UserEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserEvent(UserEventDto eventDto, String routingKey) {
        try {
            log.info("Enviando evento de usuario [{}]: ID={}", eventDto.getEventType(), eventDto.getIdPersona());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.Exchanges.AUTH_EXCHANGE,
                    routingKey,
                    eventDto
            );
        } catch (Exception e) {
            log.error("Error al enviar evento a RabbitMQ para el usuario ID={}: {}", eventDto.getIdPersona(), e.getMessage());

        }
    }
}