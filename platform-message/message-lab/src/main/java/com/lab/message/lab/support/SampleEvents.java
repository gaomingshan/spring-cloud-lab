package com.lab.message.lab.support;

import com.lab.message.contract.EventEnvelope;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class SampleEvents {
    private SampleEvents() {
    }

    public static EventEnvelope<Map<String, Object>> orderCreated() {
        return orderCreated(null);
    }

    public static EventEnvelope<Map<String, Object>> orderCreated(String partitionKey) {
        String orderId = partitionKey == null || partitionKey.isBlank()
                ? UUID.randomUUID().toString()
                : partitionKey;
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                SampleTopics.EVENT_TYPE_ORDER_CREATED,
                "message-lab",
                "Order",
                orderId,
                partitionKey,
                null,
                Instant.now(),
                null,
                Map.of("sample", "true"),
                Map.of("orderId", orderId, "source", "message-lab")
        );
    }
}
