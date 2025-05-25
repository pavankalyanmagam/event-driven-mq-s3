//package com.code.delta.service1api.config;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Value("${rabbitmq.queue.submission}")
//    private String submissionQueueName;
//
//    @Value("${rabbitmq.queue.persistence}")
//    private String persistenceQueueName;
//
//    // Bean to declare the first queue
//    @Bean
//    public Queue submissionQueue() {
//        return new Queue(submissionQueueName, true); // true makes it durable
//    }
//
//    // Bean to declare the second queue
//    @Bean
//    public Queue persistenceQueue() {
//        return new Queue(persistenceQueueName, true);
//    }
//
//    // Bean to configure all outgoing messages to be in JSON format
//    @Bean
//    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
//        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        return rabbitTemplate;
//    }
//
//    // Bean to configure all incoming messages to be read as JSON
//    @Bean
//    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    // THIS IS THE FIX
////    @Bean
////    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
////        // Create an ObjectMapper and register the JavaTimeModule to handle modern date/time types
////        // Also enables default typing to handle complex objects like UUIDs and Maps
////        ObjectMapper objectMapper = new ObjectMapper();
////        objectMapper.registerModule(new JavaTimeModule());
////        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
////        return new Jackson2JsonMessageConverter(objectMapper);
////    }
//}