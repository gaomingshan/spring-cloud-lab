package com.lab.reliable.message.model;

import java.time.Instant;

public record OutboxMessage(
        String messageId,
        String eventType,
        String payload,
        DeliveryMode deliveryMode,
        OutboxStatus status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant lockedUntil,
        String lastError) {
}
