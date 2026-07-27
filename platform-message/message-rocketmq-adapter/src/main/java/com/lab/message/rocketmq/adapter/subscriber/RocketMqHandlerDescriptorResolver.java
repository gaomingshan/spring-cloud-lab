package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Modifier;

final class RocketMqHandlerDescriptorResolver {

    <E extends BaseEvent> RocketMqHandlerDescriptor<E> resolve(EventHandler<E> handler) {
        if (handler == null) {
            throw new MessageException("VALIDATION_FAILED: handler is required");
        }

        Class<?> handlerClass = ClassUtils.getUserClass(handler);
        EventConsumer consumer = findConsumer(handlerClass);
        Class<E> eventType = resolveEventType(handler, handlerClass);
        return new RocketMqHandlerDescriptor<>(
                requireValue(consumer.topic(), "topic"),
                requireValue(consumer.group(), "group"),
                eventType,
                handler);
    }

    private static EventConsumer findConsumer(Class<?> handlerClass) {
        EventConsumer consumer = AnnotatedElementUtils.findMergedAnnotation(handlerClass, EventConsumer.class);
        if (consumer == null) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer is required on EventHandler type "
                    + handlerClass.getName());
        }
        return consumer;
    }

    private static <E extends BaseEvent> Class<E> resolveEventType(EventHandler<E> handler, Class<?> handlerClass) {
        Class<?> resolved = ResolvableType.forClass(ClassUtils.getUserClass(handler))
                .as(EventHandler.class)
                .getGeneric(0)
                .resolve();
        if (resolved == null || resolved == BaseEvent.class
                || Modifier.isAbstract(resolved.getModifiers()) || resolved.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: EventHandler must declare a concrete event type: "
                    + handlerClass.getName());
        }
        return castEventType(resolved);
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
