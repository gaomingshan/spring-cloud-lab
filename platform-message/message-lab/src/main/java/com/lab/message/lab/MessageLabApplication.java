package com.lab.message.lab;

import com.lab.message.core.EventEnvelopeFactory;
import com.lab.message.core.MessageCoreProperties;
import com.lab.message.local.LocalMessageEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

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

    @EventListener
    public void onLocalMessage(LocalMessageEvent event) {
        // The Lab intentionally delegates local dispatch to Spring's event infrastructure.
    }
}
