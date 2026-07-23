package com.lab.message.lab.support;

import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.lab.event.model.OrderCancelModel;
import com.lab.message.lab.event.model.OrderCreateModel;
import com.lab.message.lab.event.model.OrderPaymentModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class SampleEvents {
    private static final String PRODUCER = "message-lab";

    private SampleEvents() {
    }

    public static OrderLifecycleEvent orderCreated() {
        String orderId = UUID.randomUUID().toString();
        return OrderLifecycleEvent.created(
                UUID.randomUUID().toString(),
                PRODUCER,
                new OrderCreateModel(orderId, List.of("sku-1", "sku-2"), new BigDecimal("99.50")));
    }

    public static OrderLifecycleEvent orderPaid() {
        String orderId = UUID.randomUUID().toString();
        return OrderLifecycleEvent.paid(
                UUID.randomUUID().toString(),
                PRODUCER,
                new OrderPaymentModel(orderId, "pay-" + orderId.substring(0, 8), "alipay", new BigDecimal("99.50")));
    }

    public static OrderLifecycleEvent orderCancelled() {
        String orderId = UUID.randomUUID().toString();
        return OrderLifecycleEvent.cancelled(
                UUID.randomUUID().toString(),
                PRODUCER,
                new OrderCancelModel(orderId, "user-cancelled"));
    }
}
