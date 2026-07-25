package com.lab.message.contract;

import java.time.Instant;
import java.util.Map;
import lombok.Data;

/**
 * Abstract domain event root. Not instantiable; register/consume only concrete subclasses. Serialization is owned by the
 * messaging ecosystem, not this type.
 */
@Data
public abstract class BaseEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String producer;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final String partitionKey;
    private final String idempotencyKey;
    private final Map<String, String> headers;
}
