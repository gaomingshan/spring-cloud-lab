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

@RequiredArgsConstructor
public final class RocketMqEventSubscriber implements EventSubscriber {

    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMqAdapterProperties adapterProperties;

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        register(resolveRegistration(handler));
    }

    private <E extends BaseEvent> ListenerRegistration<E> resolveRegistration(EventHandler<E> handler) {
        if (handler == null) {
            throw new MessageException("VALIDATION_FAILED: handler is required");
        }

        Class<?> userClass = ClassUtils.getUserClass(handler);
        EventConsumer consumer = findConsumer(userClass);
        Class<E> eventType = resolveConcreteEventType(handler, userClass);
        String topic = requireValue(consumer.topic(), "topic");
        String group = requireValue(consumer.group(), "group");
        RocketMQMessageListener listener = createListener(topic, group);
        String beanName = createBeanName(topic, group, eventType, handler);

        return new ListenerRegistration<>(beanName, eventType, handler, listener);
    }

    private static EventConsumer findConsumer(Class<?> userClass) {
        EventConsumer consumer = AnnotatedElementUtils.findMergedAnnotation(userClass, EventConsumer.class);
        if (consumer == null) {
            throw new MessageException(
                "VALIDATION_FAILED: @EventConsumer is required on EventHandler type " + userClass.getName());
        }
        return consumer;
    }

    private static <E extends BaseEvent> Class<E> resolveConcreteEventType(
        EventHandler<E> handler, Class<?> userClass) {
        Class<E> eventType = resolveEventType(handler);
        if (eventType == null || eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers())
            || eventType.isInterface()) {
            throw new MessageException(
                "VALIDATION_FAILED: EventHandler must declare a concrete event type: " + userClass.getName());
        }
        return eventType;
    }

    private static String requireValue(String value, String attribute) {
        if (value == null || value.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer " + attribute + " is required");
        }
        return value.trim();
    }

    private RocketMQMessageListener createListener(String topic, String group) {
        var attributes = RocketMqListenerAnnotationFactory.buildAttributes(
            topic, group, adapterProperties.findConsumer(topic, group));
        return AnnotationUtils.synthesizeAnnotation(attributes, RocketMQMessageListener.class, null);
    }

    private static <E extends BaseEvent> String createBeanName(
        String topic, String group, Class<E> eventType, EventHandler<E> handler) {
        return "labRocketMqListener-" + group + "-" + topic + "-" + eventType.getSimpleName()
            + "-" + System.identityHashCode(handler);
    }

    private <E extends BaseEvent> void register(ListenerRegistration<E> registration) {
        registrar.registerContainer(
            registration.beanName(),
            new TypedListener<>(registration.eventType(), registration.handler()),
            registration.listener());
    }

    @SuppressWarnings("unchecked")
    private static <E extends BaseEvent> Class<E> resolveEventType(EventHandler<E> handler) {
        Class<?> resolved = ResolvableType.forClass(ClassUtils.getUserClass(handler))
            .as(EventHandler.class)
            .getGeneric(0)
            .resolve();
        return resolved == null ? null : (Class<E>) resolved;
    }

    private record ListenerRegistration<E extends BaseEvent>(
        String beanName,
        Class<E> eventType,
        EventHandler<E> handler,
        RocketMQMessageListener listener) {
    }

    private record TypedListener<E extends BaseEvent>(Class<E> eventType, EventHandler<E> handler) implements
        RocketMQListener<E> {

        @Override
        public void onMessage(E event) {
            if (event == null) {
                throw new MessageException("DESERIALIZE_FAILED: null event for " + eventType.getName());
            }
            if (!eventType.isInstance(event)) {
                throw new MessageException(
                    "DESERIALIZE_FAILED: expected " + eventType.getName() + " but got " + event.getClass().getName());
            }
            handler.handle(event);
        }
    }
}
