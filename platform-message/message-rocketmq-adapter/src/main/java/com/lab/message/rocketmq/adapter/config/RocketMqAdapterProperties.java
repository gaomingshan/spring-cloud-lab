package com.lab.message.rocketmq.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapter facade configuration. Consumer extras are a raw map container
 * (topic -&gt; group -&gt; attribute map). No typed consumer field model.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lab.message.adapter")
public class RocketMqAdapterProperties {
    private boolean enabled = true;
    private Map<Duration, Integer> delayLevels = new LinkedHashMap<>();
    /**
     * Optional overlay for native listener attributes: topic -&gt; group -&gt; attrs.
     * Keys should match RocketMQMessageListener attribute names.
     */
    private Map<String, Map<String, Map<String, Object>>> consumers = new LinkedHashMap<>();

    public void setDelayLevels(Map<Duration, Integer> delayLevels) {
        this.delayLevels = delayLevels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(delayLevels);
    }

    public void setConsumers(Map<String, Map<String, Map<String, Object>>> consumers) {
        this.consumers = consumers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(consumers);
    }

    public Map<String, Object> overlay(String topic, String group) {
        if (topic == null || group == null || consumers == null) {
            return Map.of();
        }
        Map<String, Map<String, Object>> byGroup = consumers.get(topic);
        if (byGroup == null) {
            return Map.of();
        }
        Map<String, Object> attrs = byGroup.get(group);
        return attrs == null ? Map.of() : Collections.unmodifiableMap(attrs);
    }
}
