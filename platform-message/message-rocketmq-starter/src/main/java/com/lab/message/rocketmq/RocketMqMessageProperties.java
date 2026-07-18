package com.lab.message.rocketmq;

import com.lab.message.contract.MessageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "lab.message.rocketmq")
public class RocketMqMessageProperties {
    private static final Pattern NAME_SERVER = Pattern.compile(
            "(?i)(?:[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?|\\[[0-9a-f:]+\\]):[1-9][0-9]{0,4}(?:,(?:[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?|\\[[0-9a-f:]+\\]):[1-9][0-9]{0,4})*");
    private boolean enabled;
    private String nameServer;
    private Producer producer = new Producer();
    private Naming naming = new Naming();
    private Transaction transaction = new Transaction();
    private Map<Duration, Integer> delayLevels = new LinkedHashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getNameServer() { return nameServer; }
    public void setNameServer(String nameServer) { this.nameServer = nameServer; }
    public Producer getProducer() { return producer; }
    public void setProducer(Producer producer) { this.producer = producer; }
    public Naming getNaming() { return naming; }
    public void setNaming(Naming naming) { this.naming = naming; }
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    public Map<Duration, Integer> getDelayLevels() { return delayLevels; }
    public void setDelayLevels(Map<Duration, Integer> delayLevels) {
        this.delayLevels = delayLevels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(delayLevels);
    }

    void validate() {
        if (producer == null || naming == null || transaction == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ settings are required");
        }
        if (nameServer == null || nameServer.isBlank() || !NAME_SERVER.matcher(nameServer.trim()).matches()) {
            throw new MessageException("CONFIGURATION_FAILED: nameServer is required");
        }
        if (producer.group == null || producer.group.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: producerGroup is required");
        }
        if (producer.sendTimeout == null || producer.sendTimeout.isZero() || producer.sendTimeout.isNegative()
                || producer.sendTimeout.toMillis() > Integer.MAX_VALUE || producer.retryTimes < 0) {
            throw new MessageException("CONFIGURATION_FAILED: producer settings are invalid");
        }
        if (naming.topicPrefix == null || naming.topicPrefix.isBlank()
                || naming.groupPrefix == null || naming.groupPrefix.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: naming prefixes are required");
        }
        if (!delayLevels.isEmpty()) {
            delayLevels.forEach((duration, level) -> {
                if (duration == null || duration.isZero() || duration.isNegative() || level == null || level <= 0) {
                    throw new MessageException("CONFIGURATION_FAILED: delay levels are invalid");
                }
            });
        }
    }

    public static class Producer {
        private String group = "lab-message-producer";
        private Duration sendTimeout = Duration.ofSeconds(3);
        private int retryTimes = 2;
        private boolean retryAnotherBroker;
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public Duration getSendTimeout() { return sendTimeout; }
        public void setSendTimeout(Duration sendTimeout) { this.sendTimeout = sendTimeout; }
        public int getRetryTimes() { return retryTimes; }
        public void setRetryTimes(int retryTimes) { this.retryTimes = retryTimes; }
        public boolean isRetryAnotherBroker() { return retryAnotherBroker; }
        public void setRetryAnotherBroker(boolean retryAnotherBroker) { this.retryAnotherBroker = retryAnotherBroker; }
    }

    public static class Naming {
        private String topicPrefix = "lab";
        private String groupPrefix = "lab";
        public String getTopicPrefix() { return topicPrefix; }
        public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
        public String getGroupPrefix() { return groupPrefix; }
        public void setGroupPrefix(String groupPrefix) { this.groupPrefix = groupPrefix; }
    }

    public static class Transaction {
        private boolean enabled;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
