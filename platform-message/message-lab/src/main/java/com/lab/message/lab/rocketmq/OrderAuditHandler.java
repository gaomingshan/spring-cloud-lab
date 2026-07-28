package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventConsumer(topic = "lab_order_created_events", group = "message-lab-order-audit")
@ConditionalOnProperty(prefix = "lab.message.rocketmq.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderAuditHandler implements EventHandler<OrderCreatedEvent> {

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("[rocketmq][consume][audit] eventId={} eventType={} producer={} thread={}",
                event.getEventId(),
                event.getEventType(),
                event.getProducer(),
                Thread.currentThread().getName());
    }
}
