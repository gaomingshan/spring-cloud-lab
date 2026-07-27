package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

record RocketMqListenerDefinition<E extends BaseEvent>(
        String beanName,
        Class<E> eventType,
        EventHandler<E> handler,
        RocketMQMessageListener listener) {
}
