package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;
import java.time.Duration;

public final class RocketMqDelayedProducer implements DelayedEventPublisher {
    private final RocketMqProducer delegate;
    public RocketMqDelayedProducer(RocketMqProducer delegate) { this.delegate = delegate; }
    @Override public PublishResult publish(EventEnvelope<?> event) { return delegate.publish(event); }
    @Override public PublishResult publish(EventEnvelope<?> event, PublishOptions options) { return delegate.publish(event, options); }
    @Override public PublishResult publishDelayed(EventEnvelope<?> event, Duration delay) { return delegate.publishDelayed(event, delay); }
}
