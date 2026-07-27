package com.lab.message.rocketmq.adapter.publisher;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.rocketmq.adapter.support.EventProducerSupport;
import com.lab.message.rocketmq.adapter.support.RocketMqMessageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

@RequiredArgsConstructor
public final class RocketMqEventPublisher implements EventPublisher {
    private final RocketMQTemplate template;
    private final RocketMqMessageMapper mapper;

    @Override
    public void publish(BaseEvent event) {
        String topic = EventProducerSupport.resolveTopic(event);
        requireOk(template.syncSend(topic, mapper.map(event)), "ordinary");
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        String partitionKey = event == null ? null : event.getPartitionKey();
        if (partitionKey == null || partitionKey.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey or aggregateId is required for ordered publishing");
        }
        String topic = EventProducerSupport.resolveTopic(event);
        Message<?> message = mapper.map(event);
        requireOk(template.syncSendOrderly(topic, message, partitionKey), "ordered");
    }

    private static void requireOk(SendResult result, String mode) {
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED");
        }
    }
}
