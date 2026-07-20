package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
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

    public Message<?> map(EventEnvelope<?> event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        return MessageBuilder.withPayload(event)
                .setHeader(RocketMQHeaders.KEYS, event.eventId())
                .setHeader(RocketMQHeaders.TAGS, event.eventType())
                .setHeader(RocketMQHeaders.TOPIC, topic(event))
                .setHeader("lab.event-id", event.eventId())
                .setHeader("lab.event-type", event.eventType())
                .setHeader("lab.producer", event.producer())
                .setHeader("lab.traceparent", event.traceparent())
                .copyHeaders(event.headers())
                .build();
    }

    public String topic(EventEnvelope<?> event) {
        String topic = destinationResolver.topic(event);
        if (topic == null || topic.isBlank()) {
            throw new MessageException("NAMING_FAILED: RocketMQ topic is blank");
        }
        return topic;
    }
}
