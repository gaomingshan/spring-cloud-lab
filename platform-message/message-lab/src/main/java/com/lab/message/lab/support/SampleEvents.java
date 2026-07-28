package com.lab.message.lab.support;

import com.lab.message.lab.event.OrderCreatedEvent;
import com.lab.message.lab.event.model.OrderCreateModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class SampleEvents {
    private static final String PRODUCER = "message-lab";

    private SampleEvents() {
    }

    public static OrderCreatedEvent orderCreated() {
        String orderId = UUID.randomUUID().toString();
        return OrderCreatedEvent.sample(
                UUID.randomUUID().toString(),
                PRODUCER,
                new OrderCreateModel(orderId, List.of("sku-1", "sku-2"), new BigDecimal("99.50")));
    }
}
