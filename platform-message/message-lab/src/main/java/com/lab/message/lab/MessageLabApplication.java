package com.lab.message.lab;

import com.lab.message.core.EventEnvelopeFactory;
import com.lab.message.core.MessageCoreProperties;
import com.lab.message.local.LocalEventHandlerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MessageLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageLabApplication.class, args);
    }

    @Bean
    EventEnvelopeFactory eventEnvelopeFactory() {
        MessageCoreProperties properties = new MessageCoreProperties();
        properties.setProducer("message-lab");
        return new EventEnvelopeFactory(properties);
    }

    @Bean
    LocalEventHandlerRegistry messageLabHandlerRegistry() {
        LocalEventHandlerRegistry registry = new LocalEventHandlerRegistry();
        registry.register("lab.message.probe.v1", event -> { });
        return registry;
    }
}
