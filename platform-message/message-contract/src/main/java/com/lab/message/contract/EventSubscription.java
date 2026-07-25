package com.lab.message.contract;

/**
 * Resolved consumer endpoint (topic + group). Built from {@link EventConsumer}, not from event types.
 */
public record EventSubscription(String topic, String group) {
    public EventSubscription {
        if (topic == null || topic.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: consumer topic is required");
        }
        if (group == null || group.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: consumer group is required");
        }
        topic = topic.trim();
        group = group.trim();
    }
}
