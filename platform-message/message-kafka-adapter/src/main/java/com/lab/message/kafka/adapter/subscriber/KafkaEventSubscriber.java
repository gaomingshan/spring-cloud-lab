package com.lab.message.kafka.adapter.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import com.lab.message.kafka.adapter.config.KafkaAdapterProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class KafkaEventSubscriber implements EventSubscriber {
    private final ConcurrentKafkaListenerContainerFactory<String, String> containerFactory;
    private final KafkaAdapterProperties adapterProperties;
    private final ObjectMapper objectMapper;
    private final List<ConcurrentMessageListenerContainer<String, String>> containers = new ArrayList<>();

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        ListenerDefinition<E> definition = resolve(handler);
        ConcurrentMessageListenerContainer<String, String> container = containerFactory.createContainer(definition.topic());
        container.getContainerProperties().setGroupId(definition.group());
        container.getContainerProperties().setMessageListener(listener(definition));
        container.setConcurrency(concurrency(definition.topic(), definition.group()));
        container.start();
        containers.add(container);
    }

    private <E extends BaseEvent> ListenerDefinition<E> resolve(EventHandler<E> handler) {
        if (handler == null) {
            throw new MessageException("VALIDATION_FAILED: handler is required");
        }
        Class<?> handlerType = ClassUtils.getUserClass(handler);
        EventConsumer consumer = handlerType.getAnnotation(EventConsumer.class);
        if (consumer == null || consumer.topic().isBlank() || consumer.group().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer topic and group are required");
        }
        Class<E> eventType = resolveEventType(handler, handlerType);
        return new ListenerDefinition<>(consumer.topic().trim(), consumer.group().trim(), eventType, handler);
    }

    private int concurrency(String topic, String group) {
        KafkaAdapterProperties.ConsumerProperties consumer = adapterProperties.findConsumer(topic, group);
        return consumer == null ? 1 : Math.max(1, consumer.getConcurrency());
    }

    private <E extends BaseEvent> MessageListener<String, String> listener(ListenerDefinition<E> definition) {
        return record -> definition.handler().handle(deserialize(record, definition.eventType()));
    }

    private <E extends BaseEvent> E deserialize(ConsumerRecord<String, String> record, Class<E> eventType) {
        try {
            return objectMapper.readValue(record.value(), eventType);
        } catch (Exception e) {
            throw new MessageException("DESERIALIZE_FAILED: Kafka record topic=" + record.topic()
                    + " partition=" + record.partition() + " offset=" + record.offset(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends BaseEvent> Class<E> resolveEventType(EventHandler<E> handler, Class<?> handlerType) {
        Class<?> eventType = org.springframework.core.ResolvableType.forClass(ClassUtils.getUserClass(handler))
                .as(EventHandler.class).getGeneric(0).resolve();
        if (eventType == null || eventType == BaseEvent.class
                || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: EventHandler must declare a concrete event type: "
                    + handlerType.getName());
        }
        return (Class<E>) eventType;
    }

    private record ListenerDefinition<E extends BaseEvent>(
            String topic, String group, Class<E> eventType, EventHandler<E> handler) {
    }
}
