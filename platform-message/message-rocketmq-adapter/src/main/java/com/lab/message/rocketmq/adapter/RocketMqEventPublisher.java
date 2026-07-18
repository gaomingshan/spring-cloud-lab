package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;

public final class RocketMqEventPublisher implements EventPublisher {
    private final RocketMqTransport transport;

    public RocketMqEventPublisher(RocketMqTransport transport) { this.transport = transport; }

    @Override
    public void publish(EventEnvelope<?> event) { transport.send(event); }
}
