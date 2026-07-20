package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;

@FunctionalInterface
public interface RocketMqDestinationResolver {
    String topic(EventEnvelope<?> event);
}
