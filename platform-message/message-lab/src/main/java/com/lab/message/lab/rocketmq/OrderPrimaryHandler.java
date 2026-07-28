package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventConsumer(topic = "lab.order-created-events", group = "message-lab-order-primary")
@ConditionalOnProperty(prefix = "lab.message.rocketmq.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderPrimaryHandler implements EventHandler<OrderCreatedEvent> {

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("[rocketmq][consume][primary] eventId={} orderId={} amount={} thread={}",
                event.getEventId(),
                event.getOrder().orderId(),
                event.getOrder().amount(),
                Thread.currentThread().getName());
    }
}
