package com.lab.message.rocketmq.adapter.support;

import com.lab.message.rocketmq.adapter.config.RocketMqAdapterProperties.ConsumerProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds a complete attribute map for {@link org.springframework.core.annotation.AnnotationUtils#synthesizeAnnotation}.
 */
public final class RocketMqListenerAnnotationFactory {
    private RocketMqListenerAnnotationFactory() {
    }

    public static Map<String, Object> buildAttributes(String topic, String group, ConsumerProperties consumer) {
        ConsumerProperties properties = consumer == null ? new ConsumerProperties() : consumer;
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("topic", topic);
        attrs.put("consumerGroup", group);
        attrs.put("selectorType", properties.getSelectorType());
        attrs.put("selectorExpression", properties.getSelectorExpression());
        attrs.put("consumeMode", properties.getConsumeMode());
        attrs.put("messageModel", properties.getMessageModel());
        attrs.put("consumeThreadMax", properties.getConsumeThreadMax());
        attrs.put("consumeThreadNumber", properties.getConsumeThreadNumber());
        attrs.put("maxReconsumeTimes", properties.getMaxReconsumeTimes());
        attrs.put("consumeTimeout", properties.getConsumeTimeout());
        attrs.put("replyTimeout", properties.getReplyTimeout());
        attrs.put("accessKey", properties.getAccessKey());
        attrs.put("secretKey", properties.getSecretKey());
        attrs.put("enableMsgTrace", properties.isEnableMsgTrace());
        attrs.put("customizedTraceTopic", properties.getCustomizedTraceTopic());
        attrs.put("nameServer", properties.getNameServer());
        attrs.put("accessChannel", properties.getAccessChannel());
        attrs.put("tlsEnable", properties.getTlsEnable());
        attrs.put("namespace", properties.getNamespace());
        attrs.put("namespaceV2", properties.getNamespaceV2());
        attrs.put("delayLevelWhenNextConsume", properties.getDelayLevelWhenNextConsume());
        attrs.put("suspendCurrentQueueTimeMillis", properties.getSuspendCurrentQueueTimeMillis());
        attrs.put("awaitTerminationMillisWhenShutdown", properties.getAwaitTerminationMillisWhenShutdown());
        attrs.put("instanceName", properties.getInstanceName());
        return attrs;
    }
}
