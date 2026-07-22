package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;

import java.time.Duration;

public final class RocketMqMessageFacade implements EventPublisher, OrderedEventPublisher,
        DelayedEventPublisher, TransactionalEventPublisher, EventSubscriber {
    private final RocketMqEventPublisher publisher;
    private final RocketMqEventSubscriber subscriber;

    public RocketMqMessageFacade(RocketMqEventPublisher publisher, RocketMqEventSubscriber subscriber) {
        if (publisher == null || subscriber == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ facade collaborators are required");
        }
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public void publish(EventEnvelope<?> event) {
        publisher.publish(event);
    }

    @Override
    public void publishOrdered(EventEnvelope<?> event) {
        publisher.publishOrdered(event);
    }

    @Override
    public void publishDelayed(EventEnvelope<?> event, Duration delay) {
        publisher.publishDelayed(event, delay);
    }

    @Override
    public void publishInTransaction(EventEnvelope<?> event) {
        publisher.publishInTransaction(event);
    }

    @Override
    public void subscribe(EventSubscription subscription, EventHandler handler) {
        subscriber.subscribe(subscription, handler);
    }
}
