package com.lab.message.contract;

import java.time.Duration;

/**
 * Unified event publishing API. Middleware-specific capabilities must be explicitly
 * implemented by an adapter; delayed delivery is unsupported by default.
 */
public interface EventPublisher {
    void publish(BaseEvent event);

    void publishOrdered(BaseEvent event);

    default void publishDelayed(BaseEvent event, Duration delay) {
        throw new MessageException("CAPABILITY_UNAVAILABLE: delayed event publishing is not supported");
    }

    void publishInTransaction(BaseEvent event);
}
