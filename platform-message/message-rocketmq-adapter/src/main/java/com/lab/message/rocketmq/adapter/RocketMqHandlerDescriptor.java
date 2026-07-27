package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;

record RocketMqHandlerDescriptor<E extends BaseEvent>(
        String topic,
        String group,
        Class<E> eventType,
        EventHandler<E> handler) {
}
