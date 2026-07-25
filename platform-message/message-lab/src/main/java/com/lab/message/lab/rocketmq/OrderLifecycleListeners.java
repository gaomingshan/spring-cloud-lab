package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.lab.event.OrderLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumer functions carry {@link EventConsumer}; auto-registered by adapter (no manual bind).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "lab.message.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderLifecycleListeners {

    @EventConsumer(topic = "lab.order-events", group = "message-lab-order-primary")
    public void onPrimary(OrderLifecycleEvent event) {
        log.info("[rocketmq:primary] phase={} eventId={} eventType={} aggregateId={} create={} payment={} cancel={}",
                event.getPhase(),
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getCreate() != null ? event.getCreate().getOrderId() : null,
                event.getPayment() != null ? event.getPayment().getPaymentId() : null,
                event.getCancel() != null ? event.getCancel().getReason() : null);
    }

    @EventConsumer(topic = "lab.order-events", group = "message-lab-order-audit")
    public void onAudit(OrderLifecycleEvent event) {
        log.info("[rocketmq:audit] phase={} eventId={} producer={}",
                event.getPhase(),
                event.getEventId(),
                event.getProducer());
    }
}
