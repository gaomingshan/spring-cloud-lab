package com.lab.message.contract;

public interface EventPublisher {
    void publish(BaseEvent event);

    /**
     * Publish to an explicit destination (topic). Prefer this when multiple concrete events
     * share one domain topic.
     */
    default void publish(String destination, BaseEvent event) {
        publish(event);
    }
}
