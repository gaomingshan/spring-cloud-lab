package com.lab.message.contract;

import java.time.Duration;

/**
 * Unified event publishing API. Capability availability is runtime/adapter concern
 * (e.g. missing delay levels or transaction listener throws MessageException).
 */
public interface EventPublisher {
    void publish(BaseEvent event);

    void publishOrdered(BaseEvent event);

    void publishDelayed(BaseEvent event, Duration delay);

    void publishInTransaction(BaseEvent event);
}
