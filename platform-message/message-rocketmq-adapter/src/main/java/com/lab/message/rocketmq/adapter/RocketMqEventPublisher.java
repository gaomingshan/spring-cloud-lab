package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

import java.time.Duration;

@RequiredArgsConstructor
public final class RocketMqEventPublisher implements EventPublisher, OrderedEventPublisher,
        DelayedEventPublisher, TransactionalEventPublisher {
    private final RocketMQTemplate template;
    private final RocketMqMessageMapper mapper;
    private final RocketMqDelayLevelResolver delayLevels;

    @Override
    public void publish(BaseEvent event) {
        String topic = EventProducerSupport.resolveTopic(event);
        requireOk(template.syncSend(topic, mapper.map(event)), "ordinary");
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        String partitionKey = event == null ? null : event.resolvePartitionKey();
        if (partitionKey == null || partitionKey.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey or aggregateId is required for ordered publishing");
        }
        String topic = EventProducerSupport.resolveTopic(event);
        Message<?> message = mapper.map(event);
        requireOk(template.syncSendOrderly(topic, message, partitionKey), "ordered");
    }

    @Override
    public void publishDelayed(BaseEvent event, Duration delay) {
        int level = delayLevels.resolve(delay);
        String topic = EventProducerSupport.resolveTopic(event);
        Message<?> message = mapper.map(event);
        long timeout = template.getProducer().getSendMsgTimeout();
        requireOk(template.syncSend(topic, message, timeout, level), "delayed");
    }

    @Override
    public void publishInTransaction(BaseEvent event) {
        try {
            String topic = EventProducerSupport.resolveTopic(event);
            template.sendMessageInTransaction(topic, mapper.map(event), event);
        } catch (MessageException e) {
            throw e;
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
