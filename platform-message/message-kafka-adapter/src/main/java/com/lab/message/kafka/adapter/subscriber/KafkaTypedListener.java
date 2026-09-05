package com.lab.message.kafka.adapter.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

/**
 * Converts the Kafka payload to the handler's concrete event type before dispatching it.
 */
@RequiredArgsConstructor
final class KafkaTypedListener<E extends BaseEvent> implements MessageListener<String, String> {
    private final Class<E> eventType;
    private final EventHandler<E> handler;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        handler.handle(deserialize(record));
    }

    private E deserialize(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            throw new MessageException("DESERIALIZE_FAILED: empty Kafka record for " + eventType.getName());
        }
        try {
            return objectMapper.readValue(record.value(), eventType);
        } catch (Exception e) {
            throw new MessageException("DESERIALIZE_FAILED: Kafka record topic=" + record.topic()
                    + " partition=" + record.partition() + " offset=" + record.offset(), e);
        }
    }
}
