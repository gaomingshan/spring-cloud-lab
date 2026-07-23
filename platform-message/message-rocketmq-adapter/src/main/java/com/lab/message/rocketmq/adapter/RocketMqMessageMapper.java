package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public final class RocketMqMessageMapper {
    private final RocketMqDestinationResolver destinationResolver;

    public RocketMqMessageMapper(RocketMqDestinationResolver destinationResolver) {
        if (destinationResolver == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ destination resolver is required");
        }
        this.destinationResolver = destinationResolver;
    }

    public Message<?> map(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        if (event.getClass() == BaseEvent.class
                || java.lang.reflect.Modifier.isAbstract(event.getClass().getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: publish requires a concrete event type");
        }
        return MessageBuilder.withPayload(event)
                .setHeader(RocketMQHeaders.KEYS, event.getEventId())
                .setHeader(RocketMQHeaders.TAGS, event.routingTag())
                .setHeader("lab.event-id", event.getEventId())
                .setHeader("lab.event-class", event.getClass().getName())
                .setHeader("lab.producer", event.getProducer())
                .copyHeaders(event.getHeaders())
                .build();
    }

    public String topic(BaseEvent event) {
        String topic = destinationResolver.topic(event);
        if (topic == null || topic.isBlank()) {
            throw new MessageException("NAMING_FAILED: RocketMQ topic is blank");
        }
        return topic;
    }
}
