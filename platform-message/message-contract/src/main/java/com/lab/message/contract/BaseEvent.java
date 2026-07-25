package com.lab.message.contract;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

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
