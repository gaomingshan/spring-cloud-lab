package com.lab.message.contract;

public interface TransactionalEventPublisher extends EventPublisher {

    void publishInTransaction(EventEnvelope<?> event);
}
