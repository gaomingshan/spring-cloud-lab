package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.DelayedEventPublisher;
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
    public void publish(BaseEvent event) {
        requireOk(template.syncSend(mapper.topic(event), mapper.map(event)), "ordinary");
    }

    @Override
    public void publish(String destination, BaseEvent event) {
        if (destination == null || destination.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: destination is required");
        }
        requireOk(template.syncSend(destination, mapper.map(event)), "ordinary");
    }

    @Override
    public void publishOrdered(BaseEvent event, String partitionKey) {
        if (partitionKey == null || partitionKey.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
        Message<?> message = mapper.map(event);
        requireOk(template.syncSendOrderly(mapper.topic(event), message, partitionKey), "ordered");
    }

    @Override
    public void publishDelayed(BaseEvent event, Duration delay) {
        int level = delayLevels.resolve(delay);
        Message<?> message = mapper.map(event);
        long timeout = template.getProducer().getSendMsgTimeout();
        requireOk(template.syncSend(mapper.topic(event), message, timeout, level), "delayed");
    }

    @Override
    public void publishInTransaction(BaseEvent event) {
        try {
            template.sendMessageInTransaction(mapper.topic(event), mapper.map(event), event);
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction send failed", e);
        }
    }

    private static void requireOk(SendResult result, String mode) {
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED");
        }
    }
}
