package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventDestination;
import com.lab.message.contract.MessageException;

public final class EventDestinationSupport {
    private EventDestinationSupport() {
    }

    public static String resolve(BaseEvent event, RocketMqDestinationResolver fallback) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        Class<?> type = event.getClass();
        if (type == BaseEvent.class || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: publish requires a concrete event type");
        }
        EventDestination annotation = type.getAnnotation(EventDestination.class);
        if (annotation != null) {
            String value = annotation.value();
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        if (fallback != null) {
            String topic = fallback.topic(event);
            if (topic != null && !topic.isBlank()) {
                return topic;
            }
        }
        throw new MessageException("CONFIGURATION_FAILED: missing @EventDestination on " + type.getName());
    }
}
