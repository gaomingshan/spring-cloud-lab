package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import com.lab.message.rocketmq.adapter.config.RocketMqAdapterProperties;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public final class RocketMqEventSubscriber implements EventSubscriber {
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMqAdapterProperties adapterProperties;

    @Override
    public <E extends BaseEvent> void bind(Class<E> eventType, String topic, String group, EventHandler<E> handler) {
        if (eventType == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: eventType and handler are required");
        }
        if (eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: bind requires a concrete event type, not " + eventType.getName());
        }
        if (topic == null || topic.isBlank() || group == null || group.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: topic and group are required");
        }
        topic = topic.trim();
        group = group.trim();
        Map<String, Object> attrs = RocketMqListenerAnnotationFactory.buildAttributes(
                topic, group, adapterProperties.overlay(topic, group));
        RocketMQMessageListener annotation = AnnotationUtils.synthesizeAnnotation(
                attrs, RocketMQMessageListener.class, null);
        String beanName = "labRocketMqListener-" + group + "-" + topic + "-" + eventType.getSimpleName();
        registrar.registerContainer(beanName, new TypedListener<>(eventType, handler), annotation);
    }

    @RequiredArgsConstructor
    private static final class TypedListener<E extends BaseEvent> implements RocketMQListener<E> {
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
}
