package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.TransactionalEventPublisher;

public final class RocketMqTransactionalProducer implements TransactionalEventPublisher {
    private final RocketMqTransport transport;

    public RocketMqTransactionalProducer(RocketMqTransport transport) { this.transport = transport; }

    @Override public void publish(EventEnvelope<?> event) { transport.send(event); }
    @Override public void publishInTransaction(EventEnvelope<?> event) { transport.sendInTransaction(event); }
}
