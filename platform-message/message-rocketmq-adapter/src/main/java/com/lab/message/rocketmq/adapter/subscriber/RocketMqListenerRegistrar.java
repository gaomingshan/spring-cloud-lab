package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.messaging.converter.MessageConverter;

@RequiredArgsConstructor
final class RocketMqListenerRegistrar {
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final MessageConverter messageConverter;

    <E extends BaseEvent> void register(RocketMqListenerDefinition<E> definition) {
        registrar.registerContainer(
                definition.beanName(),
                new RocketMqTypedListener<>(definition.eventType(), definition.handler(), messageConverter),
                definition.listener());
    }
}
