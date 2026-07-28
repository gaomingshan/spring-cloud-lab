package com.lab.message.kafka.adapter.support;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventProducer;
import com.lab.message.contract.MessageException;

public final class KafkaEventSupport {
    private KafkaEventSupport() {
    }

    public static String resolveTopic(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        Class<?> eventType = event.getClass();
        if (java.lang.reflect.Modifier.isAbstract(eventType.getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: publish requires a concrete event type");
        }
        EventProducer producer = eventType.getAnnotation(EventProducer.class);
        if (producer == null || producer.topic() == null || producer.topic().isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: missing @EventProducer topic on " + eventType.getName());
        }
        return producer.topic().trim();
    }

    public static String requirePartitionKey(BaseEvent event) {
        String partitionKey = event == null ? null : event.getPartitionKey();
        if (partitionKey == null || partitionKey.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
        return partitionKey;
    }
}
