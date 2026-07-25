package com.lab.message.rocketmq.adapter;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Builds attribute map for {@link org.springframework.core.annotation.AnnotationUtils#synthesizeAnnotation}.
 * Overlay keys should match {@link RocketMQMessageListener} attribute names (camelCase).
 */
public final class RocketMqListenerAnnotationFactory {
    private RocketMqListenerAnnotationFactory() {
    }

    public static Map<String, Object> buildAttributes(String topic, String group, Map<String, Object> overlay) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("topic", topic);
        attrs.put("consumerGroup", group);
        attrs.put("selectorType", SelectorType.TAG);
        attrs.put("selectorExpression", "*");
        attrs.put("consumeMode", ConsumeMode.CONCURRENTLY);
        attrs.put("messageModel", MessageModel.CLUSTERING);
        attrs.put("consumeThreadMax", 64);
        attrs.put("consumeThreadNumber", 20);
        attrs.put("maxReconsumeTimes", -1);
        attrs.put("consumeTimeout", 15L);
        attrs.put("replyTimeout", 3000);
        attrs.put("accessKey", RocketMQMessageListener.ACCESS_KEY_PLACEHOLDER);
        attrs.put("secretKey", RocketMQMessageListener.SECRET_KEY_PLACEHOLDER);
        attrs.put("enableMsgTrace", false);
        attrs.put("customizedTraceTopic", RocketMQMessageListener.TRACE_TOPIC_PLACEHOLDER);
        attrs.put("nameServer", RocketMQMessageListener.NAME_SERVER_PLACEHOLDER);
        attrs.put("accessChannel", RocketMQMessageListener.ACCESS_CHANNEL_PLACEHOLDER);
        attrs.put("tlsEnable", "false");
        attrs.put("namespace", "");
        attrs.put("namespaceV2", "");
        attrs.put("delayLevelWhenNextConsume", 0);
        attrs.put("suspendCurrentQueueTimeMillis", 1000);
        attrs.put("awaitTerminationMillisWhenShutdown", 1000);
        attrs.put("instanceName", "DEFAULT");

        if (overlay != null) {
            for (Map.Entry<String, Object> e : overlay.entrySet()) {
                String key = e.getKey();
                if (key == null || key.isBlank()) {
                    continue;
                }
                // annotation wins for identity
                if ("topic".equals(key) || "consumerGroup".equals(key)) {
                    continue;
                }
                Object coerced = coerce(key, e.getValue());
                if (coerced != null) {
                    attrs.put(key, coerced);
                }
            }
        }
        return attrs;
    }

    private static Object coerce(String key, Object raw) {
        if (raw == null) {
            return null;
        }
        if ("consumeMode".equals(key)) {
            return toEnum(ConsumeMode.class, raw, ConsumeMode.CONCURRENTLY);
        }
        if ("messageModel".equals(key)) {
            return toEnum(MessageModel.class, raw, MessageModel.CLUSTERING);
        }
        if ("selectorType".equals(key)) {
            return toEnum(SelectorType.class, raw, SelectorType.TAG);
        }
        if (raw instanceof Number n) {
            return switch (key) {
                case "consumeTimeout" -> n.longValue();
                case "consumeThreadMax", "consumeThreadNumber", "maxReconsumeTimes", "replyTimeout",
                        "delayLevelWhenNextConsume", "suspendCurrentQueueTimeMillis",
                        "awaitTerminationMillisWhenShutdown" -> n.intValue();
                default -> raw;
            };
        }
        if (raw instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            return switch (key) {
                case "consumeMode" -> toEnum(ConsumeMode.class, t, ConsumeMode.CONCURRENTLY);
                case "messageModel" -> toEnum(MessageModel.class, t, MessageModel.CLUSTERING);
                case "selectorType" -> toEnum(SelectorType.class, t, SelectorType.TAG);
                case "enableMsgTrace" -> Boolean.parseBoolean(t);
                case "consumeTimeout" -> Long.parseLong(t);
                case "consumeThreadMax", "consumeThreadNumber", "maxReconsumeTimes", "replyTimeout",
                        "delayLevelWhenNextConsume", "suspendCurrentQueueTimeMillis",
                        "awaitTerminationMillisWhenShutdown" -> Integer.parseInt(t);
                default -> t;
            };
        }
        if (raw instanceof Boolean) {
            return raw;
        }
        return raw;
    }

    private static <E extends Enum<E>> E toEnum(Class<E> type, Object raw, E defaultValue) {
        if (raw instanceof Enum<?> e && type.isInstance(e)) {
            return type.cast(e);
        }
        String name = String.valueOf(raw).trim().toUpperCase(Locale.ROOT);
        // allow CONCURRENT as alias
        if (type == ConsumeMode.class && "CONCURRENT".equals(name)) {
            name = "CONCURRENTLY";
        }
        if (type == ConsumeMode.class && "ORDERED".equals(name)) {
            name = "ORDERLY";
        }
        try {
            return Enum.valueOf(type, name);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
