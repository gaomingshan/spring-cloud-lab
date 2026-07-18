package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;
import com.lab.message.contract.TransactionalEventPublisher;
public final class RocketMqTransactionalProducer implements TransactionalEventPublisher {
    private final RocketMqProducer delegate;
    public RocketMqTransactionalProducer(RocketMqProducer delegate) {
        this.delegate = delegate;
    }
    @Override public PublishResult publish(EventEnvelope<?> event) { return delegate.publish(event); }
    @Override public PublishResult publish(EventEnvelope<?> event, PublishOptions options) { return delegate.publish(event, options); }
    @Override public PublishResult publishTransactional(EventEnvelope<?> event) {
        return delegate.publishTransactional(event);
    }
}
