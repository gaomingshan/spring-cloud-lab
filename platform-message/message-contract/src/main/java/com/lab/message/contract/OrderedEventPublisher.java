package com.lab.message.contract;

public interface OrderedEventPublisher extends EventPublisher {

    void publishOrdered(EventEnvelope<?> event);
}
