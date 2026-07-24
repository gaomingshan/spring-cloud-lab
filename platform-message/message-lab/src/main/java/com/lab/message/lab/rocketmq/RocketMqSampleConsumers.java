package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventSubscriber;
import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.lab.support.SampleBindings;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumer channel (destination/group) comes from lab.message.consumers.* configuration.
 * Producer {@code @EventDestination} is intentionally independent.
 */
@Component
@ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "true")
@ConditionalOnBean(EventSubscriber.class)
public class RocketMqSampleConsumers {
    private static final Logger log = LoggerFactory.getLogger(RocketMqSampleConsumers.class);

    private final EventSubscriber subscriber;

    public RocketMqSampleConsumers(EventSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    @PostConstruct
    public void bind() {
        subscriber.bind(SampleBindings.ORDER_PRIMARY, OrderLifecycleEvent.class, event -> log.info(
                "[rocketmq:primary] phase={} eventId={} eventType={} aggregateId={} create={} payment={} cancel={}",
                event.getPhase(),
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getCreate() != null ? event.getCreate().getOrderId() : null,
                event.getPayment() != null ? event.getPayment().getPaymentId() : null,
                event.getCancel() != null ? event.getCancel().getReason() : null));

        subscriber.bind(SampleBindings.ORDER_AUDIT, OrderLifecycleEvent.class, event -> log.info(
                "[rocketmq:audit] phase={} eventId={} producer={}",
                event.getPhase(),
                event.getEventId(),
                event.getProducer()));

        log.info("[rocketmq] bound consumers {} and {} to {}",
                SampleBindings.ORDER_PRIMARY, SampleBindings.ORDER_AUDIT, OrderLifecycleEvent.class.getName());
    }
}
