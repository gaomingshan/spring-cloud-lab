package com.lab.message.rocketmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Platform personalization for <strong>native</strong> Apache RocketMQ Spring Boot.
 * <p>
 * Official keys live under {@code rocketmq.*} (see {@link org.apache.rocketmq.spring.autoconfigure.RocketMQProperties}).
 * This type documents and supplies lab defaults that are applied only when the corresponding
 * official property is absent — so you can learn RocketMQ core knobs while keeping a thin platform layer.
 *
 * <pre>
 * rocketmq.name-server              &lt;- nameServer / defaults
 * rocketmq.access-channel           &lt;- accessChannel
 * rocketmq.producer.group           &lt;- producer.group
 * rocketmq.producer.send-message-timeout
 * rocketmq.producer.retry-times-when-send-failed
 * rocketmq.producer.retry-times-when-send-async-failed
 * rocketmq.producer.max-message-size
 * rocketmq.producer.enable-msg-trace
 * rocketmq.producer.tls-enable
 * rocketmq.consumer.message-model
 * rocketmq.consumer.selector-type
 * </pre>
 */
@ConfigurationProperties(prefix = "lab.message.rocketmq")
public class RocketMqNativeProperties {
    /**
     * Enable lab native personalization (defaults applicator + validation).
     * Official RocketMQ auto-config still keys off {@code rocketmq.name-server}.
     */
    private boolean enabled = true;

    /**
     * Maps to {@code rocketmq.name-server}. Format: {@code host:port;host:port}.
     */
    private String nameServer = "127.0.0.1:9876";

    /**
     * Maps to {@code rocketmq.access-channel}: LOCAL or CLOUD.
     */
    private String accessChannel;

    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public String getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(String accessChannel) {
        this.accessChannel = accessChannel;
    }

    public Producer getProducer() {
        return producer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public static class Producer {
        /**
         * Maps to {@code rocketmq.producer.group}. Empty = {@code ${spring.application.name}-producer}.
         */
        private String group = "";

        /**
         * Maps to {@code rocketmq.producer.send-message-timeout} (ms).
         */
        private Duration sendMessageTimeout = Duration.ofSeconds(3);

        /**
         * Maps to {@code rocketmq.producer.retry-times-when-send-failed}.
         */
        private int retryTimesWhenSendFailed = 2;

        /**
         * Maps to {@code rocketmq.producer.retry-times-when-send-async-failed}.
         */
        private int retryTimesWhenSendAsyncFailed = 2;

        /**
         * Maps to {@code rocketmq.producer.retry-next-server}.
         */
        private boolean retryNextServer = false;

        /**
         * Maps to {@code rocketmq.producer.max-message-size} (bytes).
         */
        private int maxMessageSize = 1024 * 1024 * 4;

        /**
         * Maps to {@code rocketmq.producer.enable-msg-trace}.
         */
        private boolean enableMsgTrace = false;

        /**
         * Maps to {@code rocketmq.producer.tls-enable}.
         */
        private boolean tlsEnable = false;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Duration getSendMessageTimeout() {
            return sendMessageTimeout;
        }

        public void setSendMessageTimeout(Duration sendMessageTimeout) {
            this.sendMessageTimeout = sendMessageTimeout;
        }

        public int getRetryTimesWhenSendFailed() {
            return retryTimesWhenSendFailed;
        }

        public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
            this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
        }

        public int getRetryTimesWhenSendAsyncFailed() {
            return retryTimesWhenSendAsyncFailed;
        }

        public void setRetryTimesWhenSendAsyncFailed(int retryTimesWhenSendAsyncFailed) {
            this.retryTimesWhenSendAsyncFailed = retryTimesWhenSendAsyncFailed;
        }

        public boolean isRetryNextServer() {
            return retryNextServer;
        }

        public void setRetryNextServer(boolean retryNextServer) {
            this.retryNextServer = retryNextServer;
        }

        public int getMaxMessageSize() {
            return maxMessageSize;
        }

        public void setMaxMessageSize(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
        }

        public boolean isEnableMsgTrace() {
            return enableMsgTrace;
        }

        public void setEnableMsgTrace(boolean enableMsgTrace) {
            this.enableMsgTrace = enableMsgTrace;
        }

        public boolean isTlsEnable() {
            return tlsEnable;
        }

        public void setTlsEnable(boolean tlsEnable) {
            this.tlsEnable = tlsEnable;
        }
    }

    public static class Consumer {
        /**
         * Maps to {@code rocketmq.consumer.message-model}: CLUSTERING or BROADCASTING.
         */
        private String messageModel = "CLUSTERING";

        /**
         * Maps to {@code rocketmq.consumer.selector-type}: TAG or SQL92.
         */
        private String selectorType = "TAG";

        public String getMessageModel() {
            return messageModel;
        }

        public void setMessageModel(String messageModel) {
            this.messageModel = messageModel;
        }

        public String getSelectorType() {
            return selectorType;
        }

        public void setSelectorType(String selectorType) {
            this.selectorType = selectorType;
        }
    }
}
