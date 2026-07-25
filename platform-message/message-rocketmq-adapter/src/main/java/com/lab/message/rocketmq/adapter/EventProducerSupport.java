package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventProducer;
import com.lab.message.contract.MessageException;

public final class EventProducerSupport {
    private EventProducerSupport() {
    }

    public static String resolveTopic(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        Class<?> type = event.getClass();
        if (type == BaseEvent.class || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: publish requires a concrete event type");
        }
        EventProducer producer = type.getAnnotation(EventProducer.class);
        if (producer == null) {
            throw new MessageException("CONFIGURATION_FAILED: missing @EventProducer on " + type.getName());
        }
        String topic = producer.topic();
        if (topic == null || topic.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: @EventProducer.topic is blank on " + type.getName());
        }
        return topic.trim();
    }
}
