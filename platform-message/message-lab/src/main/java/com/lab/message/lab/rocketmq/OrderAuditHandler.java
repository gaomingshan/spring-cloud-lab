package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderLifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventConsumer(topic = "lab.order-events", group = "message-lab-order-audit")
@ConditionalOnProperty(prefix = "lab.message.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderAuditHandler implements EventHandler<OrderLifecycleEvent> {

    @Override
    public void handle(OrderLifecycleEvent event) {
        log.info("[rocketmq:audit] phase={} eventId={} producer={}",
                event.getPhase(),
                event.getEventId(),
                event.getProducer());
    }
}
