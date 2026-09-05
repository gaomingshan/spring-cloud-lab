package com.lab.message.rocketmq.adapter.support;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** Adapts the facade endpoint identity to the two required native listener attributes. */
public final class RocketMqListenerAnnotationFactory {
    private RocketMqListenerAnnotationFactory() {
    }

    public static RocketMQMessageListener create(String topic, String group) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("topic", topic);
        attributes.put("consumerGroup", group);
        return AnnotationUtils.synthesizeAnnotation(attributes, RocketMQMessageListener.class, null);
    }
}
