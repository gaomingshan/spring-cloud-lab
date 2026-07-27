package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RocketMqMessageFacade implements EventPublisher, EventSubscriber {
    private final RocketMqEventPublisher publisher;
    private final RocketMqEventSubscriber subscriber;

    @Override
    public void publish(BaseEvent event) {
        publisher.publish(event);
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        publisher.publishOrdered(event);
    }

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        subscriber.bind(handler);
    }
}
