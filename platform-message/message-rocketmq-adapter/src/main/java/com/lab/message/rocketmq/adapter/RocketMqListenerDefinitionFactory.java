package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.rocketmq.adapter.config.RocketMqAdapterProperties;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.core.annotation.AnnotationUtils;

@RequiredArgsConstructor
final class RocketMqListenerDefinitionFactory {
    private final RocketMqAdapterProperties adapterProperties;

    <E extends BaseEvent> RocketMqListenerDefinition<E> create(RocketMqHandlerDescriptor<E> descriptor) {
        String topic = descriptor.topic();
        String group = descriptor.group();
        RocketMQMessageListener listener = AnnotationUtils.synthesizeAnnotation(
                RocketMqListenerAnnotationFactory.buildAttributes(
                        topic, group, adapterProperties.findConsumer(topic, group)),
                RocketMQMessageListener.class,
                null);
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
