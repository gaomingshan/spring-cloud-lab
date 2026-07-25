package com.lab.message.rocketmq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Platform personalization for <strong>native</strong> Apache RocketMQ Spring Boot.
 * Official keys live under {@code rocketmq.*}; this type supplies lab defaults when official
 * properties are absent.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lab.message.rocketmq")
public class RocketMqNativeProperties {
    private boolean enabled = true;
    private String nameServer = "127.0.0.1:9876";
    private String accessChannel;
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();

    @Getter
    @Setter
    public static class Producer {
        private String group = "";
        private Duration sendMessageTimeout = Duration.ofSeconds(3);
        private int retryTimesWhenSendFailed = 2;
        private int retryTimesWhenSendAsyncFailed = 2;
        private boolean retryNextServer = false;
        private int maxMessageSize = 1024 * 1024 * 4;
        private boolean enableMsgTrace = false;
        private boolean tlsEnable = false;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String messageModel = "CLUSTERING";
        private String selectorType = "TAG";
    }
}
