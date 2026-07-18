package com.lab.message.lab;

import com.lab.message.local.LocalMessageEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MessageLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageLabApplication.class, args);
    }

    @EventListener
    public void onLocalMessage(LocalMessageEvent event) {
        event.envelope().eventType();
    }
}
