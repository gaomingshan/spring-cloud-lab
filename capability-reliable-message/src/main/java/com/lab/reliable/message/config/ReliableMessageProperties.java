package com.lab.reliable.message.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("reliable.message")
public class ReliableMessageProperties {
    private boolean enabled = true;
    private final Outbox outbox = new Outbox();
    private final Processing processing = new Processing();
    private final Retry retry = new Retry();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public Processing getProcessing() {
        return processing;
    }

    public Retry getRetry() {
        return retry;
    }

    public static class Outbox {
        private int batchSize = 100;
        private Duration pollInterval = Duration.ofSeconds(1);
        private Duration leaseTimeout = Duration.ofSeconds(30);

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public Duration getPollInterval() { return pollInterval; }
        public void setPollInterval(Duration pollInterval) { this.pollInterval = pollInterval; }
        public Duration getLeaseTimeout() { return leaseTimeout; }
        public void setLeaseTimeout(Duration leaseTimeout) { this.leaseTimeout = leaseTimeout; }
    }

    public static class Processing {
        private Duration leaseTimeout = Duration.ofMinutes(5);

        public Duration getLeaseTimeout() { return leaseTimeout; }
        public void setLeaseTimeout(Duration leaseTimeout) { this.leaseTimeout = leaseTimeout; }
    }

    public static class Retry {
        private int maxAttempts = 20;
        private Duration initialDelay = Duration.ofSeconds(1);
        private Duration maxDelay = Duration.ofMinutes(5);

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public Duration getInitialDelay() { return initialDelay; }
        public void setInitialDelay(Duration initialDelay) { this.initialDelay = initialDelay; }
        public Duration getMaxDelay() { return maxDelay; }
        public void setMaxDelay(Duration maxDelay) { this.maxDelay = maxDelay; }
    }
}
