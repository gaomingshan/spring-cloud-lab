package com.lab.message.contract;

@FunctionalInterface
public interface EventHandler {
    void handle(EventEnvelope<?> event);
}
