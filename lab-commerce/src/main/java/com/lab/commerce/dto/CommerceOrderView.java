package com.lab.commerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommerceOrderView(
        Long id,
        String requestId,
        Long userId,
        Long productId,
        Integer count,
        BigDecimal money,
        String status,
        LocalDateTime createdAt,
        List<OutboxEventView> outboxEvents) {
}
