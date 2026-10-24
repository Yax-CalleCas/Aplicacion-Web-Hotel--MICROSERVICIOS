package com.reporte.hotel.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String HABITACION_QUEUE = "reporte.habitacion.queue";
    public static final String VENTA_QUEUE = "reporte.venta.queue";
    public static final String RECEPCION_QUEUE = "reporte.recepcion.queue";

    public static final String HOTEL_EXCHANGE = "hotel.exchange";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange(HOTEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue habitacionQueue() {
        return new Queue(HABITACION_QUEUE, true);
    }

    @Bean
    public Binding habitacionBinding(Queue habitacionQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(habitacionQueue).to(hotelExchange).with("habitacion.#");
    }

    @Bean
    public Queue ventaQueue() {
        return new Queue(VENTA_QUEUE, true);
    }

    @Bean
    public Binding ventaBinding(Queue ventaQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(ventaQueue).to(hotelExchange).with("venta.#");
    }

    @Bean
    public Queue recepcionQueue() {
        return new Queue(RECEPCION_QUEUE, true);
    }

    @Bean
    public Binding recepcionBinding(Queue recepcionQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(recepcionQueue).to(hotelExchange).with("recepcion.#");
    }
}