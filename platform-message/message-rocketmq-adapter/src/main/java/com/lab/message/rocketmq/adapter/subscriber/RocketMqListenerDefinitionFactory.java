package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.rocketmq.adapter.support.RocketMqListenerAnnotationFactory;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

final class RocketMqListenerDefinitionFactory {
    <E extends BaseEvent> RocketMqListenerDefinition<E> create(RocketMqHandlerDescriptor<E> descriptor) {
        RocketMQMessageListener listener = RocketMqListenerAnnotationFactory.create(
                descriptor.topic(), descriptor.group());
        return new RocketMqListenerDefinition<>(
                createBeanName(descriptor),
                descriptor.eventType(),
                descriptor.handler(),
                listener);
    }

    private static <E extends BaseEvent> String createBeanName(RocketMqHandlerDescriptor<E> descriptor) {
        return "labRocketMqListener-" + descriptor.group() + "-" + descriptor.topic() + "-"
                + descriptor.eventType().getSimpleName() + "-" + System.identityHashCode(descriptor.handler());
    }
}
