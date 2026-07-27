package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventConsumer(topic = "lab.order-events", group = "message-lab-order-primary")
@ConditionalOnProperty(prefix = "lab.message.rocketmq.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderPrimaryHandler implements EventHandler<OrderLifecycleEvent> {

    @Override
    public void handle(OrderLifecycleEvent event) {
        log.info("[rocketmq:primary] phase={} eventId={} eventType={} aggregateId={} create={} payment={} cancel={}",
                event.getPhase(),
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getCreate() != null ? event.getCreate().getOrderId() : null,
                event.getPayment() != null ? event.getPayment().getPaymentId() : null,
                event.getCancel() != null ? event.getCancel().getReason() : null);
    }
}
