package com.lab.message.kafka.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Modifier;

final class KafkaHandlerDescriptorResolver {

    <E extends BaseEvent> KafkaHandlerDescriptor<E> resolve(EventHandler<E> handler) {
        if (handler == null) {
            throw new MessageException("VALIDATION_FAILED: handler is required");
        }

        Class<?> handlerType = ClassUtils.getUserClass(handler);
        EventConsumer consumer = findConsumer(handlerType);
        return new KafkaHandlerDescriptor<>(
                requireValue(consumer.topic(), "topic"),
                requireValue(consumer.group(), "group"),
                resolveEventType(handler, handlerType),
                handler);
    }

    private static EventConsumer findConsumer(Class<?> handlerType) {
        EventConsumer consumer = AnnotatedElementUtils.findMergedAnnotation(handlerType, EventConsumer.class);
        if (consumer == null) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer is required on EventHandler type "
                    + handlerType.getName());
        }
        return consumer;
    }

    private static <E extends BaseEvent> Class<E> resolveEventType(EventHandler<E> handler, Class<?> handlerType) {
        Class<?> eventType = ResolvableType.forClass(ClassUtils.getUserClass(handler))
                .as(EventHandler.class)
                .getGeneric(0)
                .resolve();
        if (eventType == null || eventType == BaseEvent.class
                || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: EventHandler must declare a concrete event type: "
                    + handlerType.getName());
        }
        return castEventType(eventType);
    }

    private static String requireValue(String value, String attribute) {
        if (value == null || value.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer " + attribute + " is required");
        }
        return value.trim();
    }

    @SuppressWarnings("unchecked")
    private static <E extends BaseEvent> Class<E> castEventType(Class<?> eventType) {
        return (Class<E>) eventType;
    }
}
