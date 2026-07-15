package com.lab.commerce.dto;

import java.time.LocalDateTime;

public record OutboxEventView(
        Long id,
        String eventType,
        String status,
        Integer retryCount,
        LocalDateTime createdAt,
        LocalDateTime publishedAt) {
}
