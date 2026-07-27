package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQListener;

@RequiredArgsConstructor
final class RocketMqTypedListener<E extends BaseEvent> implements RocketMQListener<E> {
    private final Class<E> eventType;
    private final EventHandler<E> handler;

    @Override
    public void onMessage(E event) {
        if (event == null) {
            throw new MessageException("DESERIALIZE_FAILED: null event for " + eventType.getName());
        }
        if (!eventType.isInstance(event)) {
            throw new MessageException("DESERIALIZE_FAILED: expected " + eventType.getName()
                    + " but got " + event.getClass().getName());
        }
        handler.handle(event);
    }
}
