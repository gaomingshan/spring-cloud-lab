package com.lab.message.lab.rocketmq;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.lab.support.SampleTopics;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Registers the concrete event type only (never BaseEvent).
 * Two groups on the same topic demonstrate group-level fan-out.
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
        bindGroup(SampleTopics.GROUP_PRIMARY, "primary");
        bindGroup(SampleTopics.GROUP_AUDIT, "audit");
    }

    private void bindGroup(String consumerGroup, String label) {
        EventSubscription subscription = new EventSubscription(
                SampleTopics.ORDER_EVENTS,
                consumerGroup,
                "*",
                ConsumptionMode.CONCURRENT
        );
        subscriber.subscribe(subscription, OrderLifecycleEvent.class, event -> log.info(
                "[rocketmq:{}] group={} phase={} eventId={} createOrderId={} paymentId={} cancelReason={}",
                label,
                consumerGroup,
                event.getPhase(),
                event.getEventId(),
                event.getCreate() != null ? event.getCreate().getOrderId() : null,
                event.getPayment() != null ? event.getPayment().getPaymentId() : null,
                event.getCancel() != null ? event.getCancel().getReason() : null));
        log.info("[rocketmq] subscribed label={} group={} topic={} eventType={}",
                label, consumerGroup, SampleTopics.ORDER_EVENTS, OrderLifecycleEvent.class.getName());
    }
}
