package com.code.delta.service2persistence.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    /**
     * This configures Spring JMS to use Jackson for converting messages to/from JSON.
     * It sends messages as TextMessage with a JSON payload, which is more robust
     * and avoids Java serialization security issues like "Forbidden class".
     * @return a configured message converter
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}