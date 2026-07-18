package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;

import java.time.Duration;

public final class RocketMqDelayedProducer implements DelayedEventPublisher {
    private final RocketMqTransport transport;

    public RocketMqDelayedProducer(RocketMqTransport transport) { this.transport = transport; }

    @Override public void publish(EventEnvelope<?> event) { transport.send(event); }
    @Override public void publishDelayed(EventEnvelope<?> event, Duration delay) { transport.sendDelayed(event, delay); }
}
