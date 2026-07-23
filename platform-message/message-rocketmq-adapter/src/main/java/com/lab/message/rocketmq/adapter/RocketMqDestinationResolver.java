package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;

@FunctionalInterface
public interface RocketMqDestinationResolver {
    String topic(BaseEvent event);
}
