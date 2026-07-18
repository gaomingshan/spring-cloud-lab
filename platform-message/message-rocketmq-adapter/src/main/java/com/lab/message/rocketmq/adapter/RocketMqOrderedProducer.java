package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.OrderedEventPublisher;

public final class RocketMqOrderedProducer implements OrderedEventPublisher {
    private final RocketMqTransport transport;

    public RocketMqOrderedProducer(RocketMqTransport transport) { this.transport = transport; }

    @Override public void publish(EventEnvelope<?> event) { transport.send(event); }
    @Override public void publishOrdered(EventEnvelope<?> event) { transport.sendOrdered(event); }
}
