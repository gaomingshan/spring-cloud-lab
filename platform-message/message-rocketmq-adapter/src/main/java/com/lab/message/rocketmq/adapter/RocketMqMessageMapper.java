package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public final class RocketMqMessageMapper {
    public Message<?> map(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        if (event.getClass() == BaseEvent.class
                || java.lang.reflect.Modifier.isAbstract(event.getClass().getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: publish requires a concrete event type");
        }
        return MessageBuilder.withPayload(event)
                .setHeader(RocketMQHeaders.KEYS, event.resolveIdempotencyKey())
                .setHeader("lab.event-id", event.getEventId())
                .setHeader("lab.event-class", event.getClass().getName())
                .setHeader("lab.event-type", event.getEventType())
                .setHeader("lab.producer", event.getProducer())
                .setHeader("lab.aggregate-type", event.getAggregateType())
                .setHeader("lab.aggregate-id", event.getAggregateId())
                .copyHeaders(event.getHeaders())
                .build();
    }
}
