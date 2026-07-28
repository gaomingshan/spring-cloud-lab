package com.lab.message.kafka.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "lab.message.kafka.adapter")
public class KafkaAdapterProperties {
    private boolean enabled;
    private Map<String, Map<String, ConsumerProperties>> consumers = new LinkedHashMap<>();

    public ConsumerProperties findConsumer(String topic, String group) {
        Map<String, ConsumerProperties> byGroup = consumers.get(topic);
        return byGroup == null ? null : byGroup.get(group);
    }

    @Getter
    @Setter
    public static class ConsumerProperties {
        private int concurrency = 1;
    }
}
