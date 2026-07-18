package com.lab.message.contract;

public interface EventPublisher {

    PublishResult publish(EventEnvelope<?> event);

    PublishResult publish(EventEnvelope<?> event, PublishOptions options);
}
