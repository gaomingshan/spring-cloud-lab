package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.util.Map;

public final class RocketMqTransport {
    private final RocketMQTemplate template;
    private final RocketMqMessageMapper mapper;
    private final Map<Duration, Integer> delayLevels;

    public RocketMqTransport(RocketMQTemplate template, RocketMqMessageMapper mapper,
                             Map<Duration, Integer> delayLevels) {
        if (template == null || mapper == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ template and mapper are required");
        }
        this.template = template;
        this.mapper = mapper;
        this.delayLevels = delayLevels == null ? Map.of() : Map.copyOf(delayLevels);
    }

    public void send(EventEnvelope<?> event) {
        send(mapper.topic(event), mapper.map(event));
    }

    public void sendOrdered(EventEnvelope<?> event) {
        if (event == null || event.partitionKey() == null || event.partitionKey().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
        check(sendOrderly(mapper.topic(event), mapper.map(event), event.partitionKey()), "ordered");
    }

    public void sendDelayed(EventEnvelope<?> event, Duration delay) {
        Integer level = delay == null ? null : delayLevels.get(delay);
        if (level == null) {
            throw new MessageException("VALIDATION_FAILED: delay is not configured");
        }
        check(template.syncSend(mapper.topic(event), mapper.map(event),
                template.getProducer().getSendMsgTimeout(), level), "delayed");
    }

    public void sendInTransaction(EventEnvelope<?> event) {
        try {
            template.sendMessageInTransaction(mapper.topic(event), mapper.map(event), event);
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction send failed", e);
        }
    }

    private void send(String destination, Message<?> message) {
        try {
            check(template.syncSend(destination, message), "ordinary");
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_SEND_FAILED: message send failed", e);
        }
    }

    private SendResult sendOrderly(String destination, Message<?> message, String partitionKey) {
        try {
            return template.syncSendOrderly(destination, message, partitionKey);
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_ORDERED_SEND_FAILED: ordered send failed", e);
        }
    }

    private static void check(SendResult result, String mode) {
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED");
        }
    }
}
