package com.lab.message.rocketmq.adapter.support;

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
        return MessageBuilder.withPayload(event)
            .setHeader(RocketMQHeaders.KEYS, event.getEventId())
            .setHeader(RocketMqMessageHeaders.EVENT_ID, event.getEventId())
            .setHeader(RocketMqMessageHeaders.EVENT_CLASS, event.getClass().getName())
            .setHeader(RocketMqMessageHeaders.EVENT_TYPE, event.getEventType())
            .setHeader(RocketMqMessageHeaders.PRODUCER, event.getProducer())
            .setHeader(RocketMqMessageHeaders.AGGREGATE_TYPE, event.getAggregateType())
            .setHeader(RocketMqMessageHeaders.AGGREGATE_ID, event.getAggregateId())
            .copyHeaders(event.getHeaders())
            .build();
    }
}
