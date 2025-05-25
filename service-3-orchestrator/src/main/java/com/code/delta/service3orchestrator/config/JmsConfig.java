//package com.code.delta.service3orchestrator.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
//import org.springframework.jms.support.converter.MessageConverter;
//import org.springframework.jms.support.converter.MessageType;
//
//@Configuration
//public class JmsConfig {
//
//    /**
//     * This configures Spring JMS to use Jackson for converting messages to/from JSON.
//     * This ensures the consumer (Service 3) knows how to read the JSON message
//     * sent by the producer (Service 2).
//     * @return a configured message converter
//     */
//    @Bean
//    public MessageConverter jacksonJmsMessageConverter() {
//        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
//        converter.setTargetType(MessageType.TEXT);
//        converter.setTypeIdPropertyName("_type");
//        return converter;
//    }
//}