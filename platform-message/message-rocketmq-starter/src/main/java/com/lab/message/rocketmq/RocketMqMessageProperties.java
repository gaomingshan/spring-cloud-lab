package com.lab.message.rocketmq;

import com.lab.message.contract.MessageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "lab.message.rocketmq")
public class RocketMqMessageProperties {
    private boolean enabled;
    private Naming naming = new Naming();
    private Map<Duration, Integer> delayLevels = new LinkedHashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Naming getNaming() { return naming; }
    public void setNaming(Naming naming) { this.naming = naming; }
    public Map<Duration, Integer> getDelayLevels() { return delayLevels; }
    public void setDelayLevels(Map<Duration, Integer> delayLevels) {
        this.delayLevels = delayLevels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(delayLevels);
    }

    void validate() {
        if (naming == null || naming.topicPrefix == null || naming.topicPrefix.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ topic prefix is required");
        }
        delayLevels.forEach((duration, level) -> {
            if (duration == null || duration.isZero() || duration.isNegative() || level == null || level <= 0) {
                throw new MessageException("CONFIGURATION_FAILED: RocketMQ delay levels are invalid");
            }
        });
    }

    public static class Naming {
        private String topicPrefix = "lab.";

        public String getTopicPrefix() { return topicPrefix; }
        public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
    }
}
