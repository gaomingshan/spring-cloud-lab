package com.lab.message.rocketmq;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.MessageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "lab.message")
public class RocketMqMessageProperties {
    private final Rocketmq rocketmq = new Rocketmq();
    private Map<String, ConsumerBinding> consumers = new LinkedHashMap<>();

    public Rocketmq getRocketmq() {
        return rocketmq;
    }

    public Map<String, ConsumerBinding> getConsumers() {
        return consumers;
    }

    public void setConsumers(Map<String, ConsumerBinding> consumers) {
        this.consumers = consumers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(consumers);
    }

    void validate() {
        rocketmq.validate();
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

    public static class Rocketmq {
        private boolean enabled;
        private Naming naming = new Naming();
        private Map<Duration, Integer> delayLevels = new LinkedHashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Naming getNaming() {
            return naming;
        }

        public void setNaming(Naming naming) {
            this.naming = naming;
        }

        public Map<Duration, Integer> getDelayLevels() {
            return delayLevels;
        }

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

            public String getTopicPrefix() {
                return topicPrefix;
            }

            public void setTopicPrefix(String topicPrefix) {
                this.topicPrefix = topicPrefix;
            }
        }
    }

    public static class ConsumerBinding {
        private String destination;
        private String group;
        private ConsumptionMode mode = ConsumptionMode.CONCURRENT;
        private boolean enabled = true;

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public ConsumptionMode getMode() {
            return mode;
        }

        public void setMode(ConsumptionMode mode) {
            this.mode = mode;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        void validate(String name) {
            if (!enabled) {
                return;
            }
            if (destination == null || destination.isBlank()) {
                throw new MessageException("CONFIGURATION_FAILED: consumers." + name + ".destination is required");
            }
            if (group == null || group.isBlank()) {
                throw new MessageException("CONFIGURATION_FAILED: consumers." + name + ".group is required");
            }
        }
    }
}
