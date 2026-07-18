package com.lab.message.contract;

public interface EventPublisher {

    void publish(EventEnvelope<?> event);
}
