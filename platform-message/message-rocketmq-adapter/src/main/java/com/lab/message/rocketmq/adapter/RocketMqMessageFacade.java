package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.DelayedEventPublisher;
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
    public void publish(BaseEvent event) {
        publisher.publish(event);
    }

    @Override
    public void publish(String destination, BaseEvent event) {
        publisher.publish(destination, event);
    }

    @Override
    public void publishOrdered(BaseEvent event, String partitionKey) {
        publisher.publishOrdered(event, partitionKey);
    }

    @Override
    public void publishDelayed(BaseEvent event, Duration delay) {
        publisher.publishDelayed(event, delay);
    }

    @Override
    public void publishInTransaction(BaseEvent event) {
        publisher.publishInTransaction(event);
    }

    @Override
    public <E extends BaseEvent> void subscribe(EventSubscription subscription,
                                                Class<E> eventType,
                                                EventHandler<E> handler) {
        subscriber.subscribe(subscription, eventType, handler);
    }
}
