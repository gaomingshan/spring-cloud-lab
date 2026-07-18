package com.lab.message.contract;

public interface OrderedEventPublisher extends EventPublisher {

    PublishResult publishOrdered(EventEnvelope<?> event, String partitionKey);
}
