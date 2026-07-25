package com.lab.message.rocketmq.adapter.config;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.MessageException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapter-layer (message-contract facade) configuration only.
 * Native RocketMQ client settings remain under {@code rocketmq.*} / lab native personalization.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lab.message.adapter")
public class RocketMqAdapterProperties {
    private boolean enabled = true;
    private Naming naming = new Naming();
    private Map<Duration, Integer> delayLevels = new LinkedHashMap<>();
    private Map<String, ConsumerBinding> consumers = new LinkedHashMap<>();

    public void setDelayLevels(Map<Duration, Integer> delayLevels) {
        this.delayLevels = delayLevels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(delayLevels);
    }

    public void setConsumers(Map<String, ConsumerBinding> consumers) {
        this.consumers = consumers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(consumers);
    }

    public void setNaming(Naming naming) {
        this.naming = naming == null ? new Naming() : naming;
    }

    public void validate() {
        if (naming == null || naming.topicPrefix == null || naming.topicPrefix.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: adapter naming.topic-prefix is required");
        }
        delayLevels.forEach((duration, level) -> {
            if (duration == null || duration.isZero() || duration.isNegative() || level == null || level <= 0) {
                throw new MessageException("CONFIGURATION_FAILED: adapter delay-levels are invalid");
            }
        });
        consumers.forEach((name, binding) -> {
            if (name == null || name.isBlank()) {
                throw new MessageException("CONFIGURATION_FAILED: consumer binding name is blank");
            }
            if (binding == null) {
                throw new MessageException("CONFIGURATION_FAILED: consumer binding is null: " + name);
            }
            binding.validate(name);
        });
    }

    @Getter
    @Setter
    public static class Naming {
        private String topicPrefix = "lab.";
    }

    @Getter
    @Setter
    public static class ConsumerBinding {
        private String destination;
        private String group;
        private ConsumptionMode mode = ConsumptionMode.CONCURRENT;
        private boolean enabled = true;

        void validate(String name) {
            if (!enabled) {
                return;
            }
            if (destination == null || destination.isBlank()) {
                throw new MessageException("CONFIGURATION_FAILED: lab.message.adapter.consumers." + name + ".destination is required");
            }
            if (group == null || group.isBlank()) {
                throw new MessageException("CONFIGURATION_FAILED: lab.message.adapter.consumers." + name + ".group is required");
            }
        }
    }
}
