package com.auth.seguridad.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static class Exchanges {
        public static final String AUTH_EXCHANGE = "auth.exchange";
    }

    public static class RoutingKeys {
        public static final String USER_CREATED = "auth.user.created";
        public static final String USER_UPDATED = "auth.user.updated";
        public static final String USER_DELETED = "auth.user.deleted";
    }

    public static class Queues {
        public static final String USER_SYNC_QUEUE = "auth.user.sync.queue";
    }

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(Exchanges.AUTH_EXCHANGE, true, false);
    }

    @Bean
    public Queue userSyncQueue() {
        return new Queue(Queues.USER_SYNC_QUEUE, true);
    }

    @Bean
    public Binding bindingUserCreated(Queue userSyncQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userSyncQueue).to(authExchange).with("auth.user.#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}