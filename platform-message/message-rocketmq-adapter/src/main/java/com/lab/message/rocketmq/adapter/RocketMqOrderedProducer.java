package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;

public final class RocketMqOrderedProducer implements OrderedEventPublisher {
    private final RocketMqProducer delegate;
    public RocketMqOrderedProducer(RocketMqProducer delegate) { this.delegate = delegate; }
    @Override public PublishResult publish(EventEnvelope<?> event) { return delegate.publish(event); }
    @Override public PublishResult publish(EventEnvelope<?> event, PublishOptions options) { return delegate.publish(event, options); }
    @Override public PublishResult publishOrdered(EventEnvelope<?> event, String partitionKey) { return delegate.publishOrdered(event, partitionKey); }
}
