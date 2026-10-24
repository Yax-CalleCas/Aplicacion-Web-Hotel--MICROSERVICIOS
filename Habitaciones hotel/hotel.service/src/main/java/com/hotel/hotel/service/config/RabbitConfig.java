package com.hotel.hotel.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Exchange original para tus eventos salientes de hotel
    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange("hotel.exchange", true, false);
    }

    // --- NUEVO: Configuración para escuchar los cambios de estado enviados por recepcion-service ---
    @Bean
    public Queue habitacionEstadoQueue() {
        return new Queue("habitacion.estado.cambio.queue", true);
    }

    @Bean
    public TopicExchange habitacionEstadoExchange() {
        return new TopicExchange("habitacion-estado-cambio-exchange", true, false);
    }

    @Bean
    public Binding habitacionEstadoBinding(Queue habitacionEstadoQueue, TopicExchange habitacionEstadoExchange) {
        return BindingBuilder.bind(habitacionEstadoQueue).to(habitacionEstadoExchange).with("#");
    }
}