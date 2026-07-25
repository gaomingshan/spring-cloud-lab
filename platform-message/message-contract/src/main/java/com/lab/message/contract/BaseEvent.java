package com.lab.message.contract;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Abstract domain event root. Not instantiable; register/consume only concrete subclasses.
 * Serialization is owned by the messaging ecosystem, not this type.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEvent {
    @EqualsAndHashCode.Include
    private final String eventId;
    private final Instant occurredAt;
    private final String producer;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final String partitionKey;
    private final String idempotencyKey;
    private final Map<String, String> headers;

    protected BaseEvent(String eventId,
                        Instant occurredAt,
                        String producer,
                        String eventType,
                        String aggregateType,
                        String aggregateId,
                        String partitionKey,
                        String idempotencyKey,
                        Map<String, String> headers) {
        if (eventId == null || eventId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: eventId is required");
        }
        if (occurredAt == null) {
            throw new MessageException("VALIDATION_FAILED: occurredAt is required");
        }
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.producer = producer;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.partitionKey = partitionKey;
        this.idempotencyKey = idempotencyKey;
        this.headers = headers == null || headers.isEmpty() ? Map.of() : Map.copyOf(headers);
    }

    /** Ordered send key: partitionKey, else aggregateId. */
    public String resolvePartitionKey() {
        if (partitionKey != null && !partitionKey.isBlank()) {
            return partitionKey;
        }
        if (aggregateId != null && !aggregateId.isBlank()) {
            return aggregateId;
        }
        return null;
    }

    /** Idempotency / keys hint: idempotencyKey, else eventId. */
    public String resolveIdempotencyKey() {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return idempotencyKey;
        }
        return eventId;
    }
}
