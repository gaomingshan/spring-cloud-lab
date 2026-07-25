package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import com.lab.message.rocketmq.adapter.config.RocketMqAdapterProperties;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.Map;

@RequiredArgsConstructor
public final class RocketMqEventSubscriber implements EventSubscriber {
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMqAdapterProperties adapterProperties;

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        if (handler == null) {
            throw new MessageException("VALIDATION_FAILED: handler is required");
        }
        Class<?> userClass = ClassUtils.getUserClass(handler);
        EventConsumer consumer = AnnotatedElementUtils.findMergedAnnotation(userClass, EventConsumer.class);
        if (consumer == null) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer is required on EventHandler type "
                    + userClass.getName());
        }
        Class<E> eventType = resolveEventType(handler);
        if (eventType == null || eventType == BaseEvent.class
                || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: EventHandler must declare a concrete event type: "
                    + userClass.getName());
        }
        register(eventType, consumer.topic(), consumer.group(), handler);
    }

    private <E extends BaseEvent> void register(Class<E> eventType, String topic, String group, EventHandler<E> handler) {
        if (topic == null || topic.isBlank() || group == null || group.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer topic and group are required");
        }
        topic = topic.trim();
        group = group.trim();
        Map<String, Object> attrs = RocketMqListenerAnnotationFactory.buildAttributes(
                topic, group, adapterProperties.overlay(topic, group));
        RocketMQMessageListener annotation = AnnotationUtils.synthesizeAnnotation(
                attrs, RocketMQMessageListener.class, null);
        String beanName = "labRocketMqListener-" + group + "-" + topic + "-" + eventType.getSimpleName()
                + "-" + System.identityHashCode(handler);
        registrar.registerContainer(beanName, new TypedListener<>(eventType, handler), annotation);
    }

    @SuppressWarnings("unchecked")
    private static <E extends BaseEvent> Class<E> resolveEventType(EventHandler<E> handler) {
        Class<?> resolved = ResolvableType.forClass(ClassUtils.getUserClass(handler))
                .as(EventHandler.class)
                .getGeneric(0)
                .resolve();
        if (resolved == null) {
            return null;
        }
        return (Class<E>) resolved;
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
