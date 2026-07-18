package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;
import com.lab.message.contract.TransactionalEventPublisher;
import org.apache.rocketmq.client.producer.TransactionMQProducer;

public final class RocketMqTransactionalProducer implements TransactionalEventPublisher {
    private final RocketMqProducer delegate;
    private final TransactionMQProducer transactionProducer;
    public RocketMqTransactionalProducer(RocketMqProducer delegate, TransactionMQProducer transactionProducer) {
        this.delegate = delegate;
        this.transactionProducer = transactionProducer;
    }
    @Override public PublishResult publish(EventEnvelope<?> event) { return delegate.publish(event); }
    @Override public PublishResult publish(EventEnvelope<?> event, PublishOptions options) { return delegate.publish(event, options); }
    @Override public PublishResult publishTransactional(EventEnvelope<?> event) {
        return delegate.publishTransactional(event);
    }
}
