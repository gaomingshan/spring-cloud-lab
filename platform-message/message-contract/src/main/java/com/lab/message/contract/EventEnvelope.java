package com.lab.message.contract;

import java.time.Instant;
import java.util.Map;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String producer,
        String aggregateType,
        String aggregateId,
        String partitionKey,
        String idempotencyKey,
        Instant occurredAt,
        String traceparent,
        Map<String, String> headers,
        T payload
) {
    public EventEnvelope {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}
