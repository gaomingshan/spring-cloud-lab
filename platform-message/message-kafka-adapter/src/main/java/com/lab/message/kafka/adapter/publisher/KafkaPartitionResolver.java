package com.lab.message.kafka.adapter.publisher;

import com.lab.message.contract.MessageException;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class KafkaPartitionResolver {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPartitionResolver(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public int resolve(String topic, String partitionKey) {
        List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(topic);
        if (partitions == null || partitions.isEmpty()) {
            throw new MessageException("KAFKA_METADATA_FAILED: no partitions found for topic " + topic);
        }
        return Utils.toPositive(Utils.murmur2(partitionKey.getBytes(StandardCharsets.UTF_8))) % partitions.size();
    }
}
