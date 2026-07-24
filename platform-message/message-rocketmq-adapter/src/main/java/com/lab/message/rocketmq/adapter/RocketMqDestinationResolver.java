package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;

@FunctionalInterface
public interface RocketMqDestinationResolver {
    /**
     * Fallback producer topic when {@code @EventDestination} is absent.
     */
    String topic(BaseEvent event);
}
