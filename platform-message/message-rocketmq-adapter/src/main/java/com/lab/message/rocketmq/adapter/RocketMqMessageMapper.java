package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RocketMqMessageMapper {
    private static final Set<String> RESERVED_PROPERTIES = reservedProperties();
    private static final Set<String> RESERVED_ENVELOPE_PROPERTIES = Set.of(
            "eventid", "eventtype", "traceparent");
    private final RocketMqEventCodec codec;
    private final String topicPrefix;

    public RocketMqMessageMapper(RocketMqEventCodec codec, String topicPrefix) {
        if (codec == null || blank(topicPrefix)) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ mapper dependencies are required");
        }
        this.codec = codec;
        this.topicPrefix = normalizeTopicPrefix(topicPrefix);
    }

    public Message map(EventEnvelope<?> event) {
        return map(event, null);
    }

    public Message map(EventEnvelope<?> event, Integer delayLevel) {
        if (event == null) throw new MessageException("VALIDATION_FAILED: event is null");
        String topic = topicPrefix + normalizeTopicPart(event.eventType());
        String key = event.eventId();
        Message message = new Message(topic, event.eventType(), key, codec.encode(event));
        message.putUserProperty("eventId", event.eventId());
        message.putUserProperty("eventType", event.eventType());
        if (!blank(event.traceparent())) message.putUserProperty("traceparent", event.traceparent());
        for (Map.Entry<String, String> entry : event.headers().entrySet()) {
            putHeader(message, entry, true);
        }
        if (delayLevel != null) message.setDelayTimeLevel(delayLevel);
        return message;
    }

    static boolean blank(String value) { return value == null || value.isBlank(); }

    private static String normalizeTopicPrefix(String value) {
        String normalized = value.trim();
        if (normalized.endsWith(".")) normalized = normalized.substring(0, normalized.length() - 1);
        if (blank(normalized) || !normalized.matches("[A-Za-z0-9_-]+")) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ topic prefix is malformed");
        }
        return normalized + ".";
    }

    private static String normalizeTopicPart(String value) {
        if (blank(value)) throw new MessageException("NAMING_FAILED: event type is blank");
        String normalized = value.trim().replaceAll("[^A-Za-z0-9_-]+", "-");
        if (blank(normalized)) throw new MessageException("NAMING_FAILED: event type is malformed");
        return normalized;
    }

    private void putHeader(Message message, Map.Entry<String, String> entry, boolean envelopeHeaders) {
        if (entry.getKey() == null || entry.getValue() == null) return;
        String propertyName = entry.getKey().toLowerCase(Locale.ROOT);
        if (envelopeHeaders && RESERVED_ENVELOPE_PROPERTIES.contains(propertyName)) return;
        if (RESERVED_PROPERTIES.contains(propertyName) || RESERVED_ENVELOPE_PROPERTIES.contains(propertyName)) {
            throw new MessageException("VALIDATION_FAILED: reserved RocketMQ user property: " + entry.getKey());
        }
        message.putUserProperty(entry.getKey(), entry.getValue());
    }

    private static Set<String> reservedProperties() {
        Set<String> names = new HashSet<>();
        MessageConst.STRING_HASH_SET.forEach(property -> names.add(property.toLowerCase(Locale.ROOT)));
        for (Field field : MessageConst.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class
                    && field.getName().startsWith("PROPERTY_")) {
                try {
                    names.add(((String) field.get(null)).toLowerCase(Locale.ROOT));
                } catch (IllegalAccessException ignored) {
                    // Public constants are expected to be accessible; the SDK set remains the fallback.
                }
            }
        }
        names.add("__" + "transient");
        names.add("__" + "shardingkey");
        return Set.copyOf(names);
    }
}
