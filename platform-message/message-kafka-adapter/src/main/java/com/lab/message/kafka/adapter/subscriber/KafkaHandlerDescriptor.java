package com.lab.message.kafka.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;

record KafkaHandlerDescriptor<E extends BaseEvent>(
        String topic,
        String group,
        Class<E> eventType,
        EventHandler<E> handler) {
}
