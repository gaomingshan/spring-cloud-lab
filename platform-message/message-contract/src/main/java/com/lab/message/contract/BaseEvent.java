package com.lab.message.contract;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract domain event root. Not instantiable; register and consume only concrete subclasses.
 * Serialization is owned by the messaging ecosystem (e.g. RocketMQ converter), not this type.
 */
public abstract class BaseEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final String producer;
    private final Map<String, String> headers;

    protected BaseEvent(String eventId, Instant occurredAt, String producer, Map<String, String> headers) {
        if (eventId == null || eventId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: eventId is required");
        }
        if (occurredAt == null) {
            throw new MessageException("VALIDATION_FAILED: occurredAt is required");
        }
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.producer = producer;
        this.headers = headers == null || headers.isEmpty() ? Map.of() : Map.copyOf(headers);
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getProducer() {
        return producer;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Optional routing tag for brokers that support tag/selector filtering (e.g. RocketMQ Tag).
     * Default: concrete simple class name. Override in domain events when needed.
     */
    public String routingTag() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEvent baseEvent = (BaseEvent) o;
        return eventId.equals(baseEvent.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
