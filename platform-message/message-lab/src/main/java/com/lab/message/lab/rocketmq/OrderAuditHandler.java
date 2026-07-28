package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventConsumer(topic = "lab_order_created_events", group = "message-lab-order-audit")
@Profile({"rocketmq", "kafka"})
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
