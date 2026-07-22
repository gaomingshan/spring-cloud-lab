package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

import java.time.Duration;

public final class RocketMqEventPublisher implements EventPublisher, OrderedEventPublisher,
        DelayedEventPublisher, TransactionalEventPublisher {
    private final RocketMQTemplate template;
    private final RocketMqMessageMapper mapper;
    private final RocketMqDelayLevelResolver delayLevels;

    public RocketMqEventPublisher(RocketMQTemplate template,
                                  RocketMqMessageMapper mapper,
                                  RocketMqDelayLevelResolver delayLevels) {
        if (template == null || mapper == null || delayLevels == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ publisher dependencies are required");
        }
        this.template = template;
        this.mapper = mapper;
        this.delayLevels = delayLevels;
    }

    @Override
    public void publish(EventEnvelope<?> event) {
        Message<?> message = mapper.map(event);
        requireOk(template.syncSend(mapper.topic(event), message), "ordinary");
    }

    @Override
    public void publishOrdered(EventEnvelope<?> event) {
        requirePartitionKey(event);
        Message<?> message = mapper.map(event);
        requireOk(template.syncSendOrderly(mapper.topic(event), message, event.partitionKey()), "ordered");
    }

    @Override
    public void publishDelayed(EventEnvelope<?> event, Duration delay) {
        int level = delayLevels.resolve(delay);
        Message<?> message = mapper.map(event);
        long timeout = template.getProducer().getSendMsgTimeout();
        requireOk(template.syncSend(mapper.topic(event), message, timeout, level), "delayed");
    }

    @Override
    public void publishInTransaction(EventEnvelope<?> event) {
        try {
            template.sendMessageInTransaction(mapper.topic(event), mapper.map(event), event);
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction send failed", e);
        }
    }

    private static void requirePartitionKey(EventEnvelope<?> event) {
        if (event == null || event.partitionKey() == null || event.partitionKey().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
    }

    private static void requireOk(SendResult result, String mode) {
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED");
        }
    }
}
