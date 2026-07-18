package com.lab.message.contract;

public interface TransactionalEventPublisher extends EventPublisher {

    PublishResult publishTransactional(EventEnvelope<?> event);
}
