package com.lab.message.rocketmq.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RocketMQ adapter configuration. Consumer settings are scoped by topic and consumer group.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lab.message.rocketmq.adapter")
public class RocketMqAdapterProperties {
    private boolean enabled = true;
    private Map<String, Map<String, ConsumerProperties>> consumers = new LinkedHashMap<>();

    public ConsumerProperties findConsumer(String topic, String group) {
        if (topic == null || group == null) {
            return null;
        }
        Map<String, ConsumerProperties> byGroup = consumers.get(topic);
        return byGroup == null ? null : byGroup.get(group);
    }

    @Getter
    @Setter
    public static class ConsumerProperties {
        private SelectorType selectorType = SelectorType.TAG;
        private String selectorExpression = "*";
        private ConsumeMode consumeMode = ConsumeMode.CONCURRENTLY;
        private MessageModel messageModel = MessageModel.CLUSTERING;
        private int consumeThreadMax = 64;
        private int consumeThreadNumber = 20;
        private int maxReconsumeTimes = -1;
        private long consumeTimeout = 15L;
        private int replyTimeout = 3000;
        private String accessKey = RocketMQMessageListener.ACCESS_KEY_PLACEHOLDER;
        private String secretKey = RocketMQMessageListener.SECRET_KEY_PLACEHOLDER;
        private boolean enableMsgTrace;
        private String customizedTraceTopic = RocketMQMessageListener.TRACE_TOPIC_PLACEHOLDER;
        private String nameServer = RocketMQMessageListener.NAME_SERVER_PLACEHOLDER;
        private String accessChannel = RocketMQMessageListener.ACCESS_CHANNEL_PLACEHOLDER;
        private String tlsEnable = "false";
        private String namespace = "";
        private String namespaceV2 = "";
        private int delayLevelWhenNextConsume;
        private int suspendCurrentQueueTimeMillis = 1000;
        private int awaitTerminationMillisWhenShutdown = 1000;
        private String instanceName = "DEFAULT";
    }
}
