package com.lab.message.lab.rocketmq;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.lab.support.SampleTopics;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ sample consumers: two groups on the same topic demonstrate group-level fan-out
 * (each group receives a full copy under CLUSTERING).
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
                SampleTopics.TOPIC_ORDER_CREATED,
                consumerGroup,
                "*",
                ConsumptionMode.CONCURRENT
        );
        subscriber.subscribe(subscription, envelope -> log.info(
                "[rocketmq:{}] group={} topic={} eventType={} eventId={} payload={}",
                label,
                consumerGroup,
                SampleTopics.TOPIC_ORDER_CREATED,
                envelope.eventType(),
                envelope.eventId(),
                envelope.payload()));
        log.info("[rocketmq] subscribed label={} group={} topic={}", label, consumerGroup, SampleTopics.TOPIC_ORDER_CREATED);
    }
}
