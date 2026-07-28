package com.lab.message.kafka.adapter.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.kafka.adapter.support.KafkaEventSupport;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public final class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaPartitionResolver partitionResolver;

    @Override
    public void publish(BaseEvent event) {
        String topic = KafkaEventSupport.resolveTopic(event);
        send(new ProducerRecord<>(topic, event.getEventId(), serialize(event)));
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        String topic = KafkaEventSupport.resolveTopic(event);
        String partitionKey = KafkaEventSupport.requirePartitionKey(event);
        int partition = partitionResolver.resolve(topic, partitionKey);
        send(new ProducerRecord<>(topic, partition, event.getEventId(), serialize(event)));
    }

    private void send(ProducerRecord<String, String> record) {
        try {
            kafkaTemplate.send(record).get();
        } catch (Exception e) {
            throw new MessageException("KAFKA_SEND_FAILED: topic=" + record.topic(), e);
        }
    }

    private String serialize(BaseEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new MessageException("SERIALIZATION_FAILED: " + event.getClass().getName(), e);
        }
    }
}
