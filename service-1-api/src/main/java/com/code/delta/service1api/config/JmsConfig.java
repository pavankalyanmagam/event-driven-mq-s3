//package com.code.delta.service1api.config;
//
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
//     * This ensures the producer (Service 1) sends messages in the exact same format
//     * that the consumer (Service 2) expects to receive.
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