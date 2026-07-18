package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.PublishOptions;
import com.lab.message.core.EventSerializer;
import com.lab.message.core.MessageNamingStrategy;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RocketMqMessageMapper {
    private static final Set<String> RESERVED_PROPERTIES = Set.of(
            MessageConst.STRING_HASH_SET.stream()
                    .map(property -> property.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet())
                    .toArray(String[]::new));
    private static final Set<String> RESERVED_ENVELOPE_PROPERTIES = Set.of(
            "eventid", "eventtype", "schemaversion", "traceparent");
    private final EventSerializer serializer;
    private final MessageNamingStrategy namingStrategy;

    public RocketMqMessageMapper(EventSerializer serializer, MessageNamingStrategy namingStrategy) {
        if (serializer == null || namingStrategy == null) throw new MessageException("CONFIGURATION_FAILED: mapper dependencies are required");
        this.serializer = serializer;
        this.namingStrategy = namingStrategy;
    }

    public Message map(EventEnvelope<?> event, PublishOptions options) {
        return map(event, options, null);
    }

    public Message map(EventEnvelope<?> event, PublishOptions options, Integer delayLevel) {
        if (event == null) throw new MessageException("VALIDATION_FAILED: event is null");
        PublishOptions effective = options == null ? new PublishOptions(null, null, null, Map.of()) : options;
        String topic = blank(effective.destination()) ? namingStrategy.destination(event.eventType()) : effective.destination();
        String key = blank(effective.key()) ? event.eventId() : effective.key();
        Message message = new Message(topic, event.eventType(), key, serializer.serialize(event));
        message.putUserProperty("eventId", event.eventId());
        message.putUserProperty("eventType", event.eventType());
        message.putUserProperty("schemaVersion", Integer.toString(event.schemaVersion()));
        if (!blank(event.traceparent())) message.putUserProperty("traceparent", event.traceparent());
        for (Map.Entry<String, String> entry : event.headers().entrySet()) {
            putHeader(message, entry);
        }
        for (Map.Entry<String, String> entry : effective.headers().entrySet()) {
            putHeader(message, entry);
        }
        if (delayLevel != null) message.setDelayTimeLevel(delayLevel);
        return message;
    }

    static boolean blank(String value) { return value == null || value.isBlank(); }

    private void putHeader(Message message, Map.Entry<String, String> entry) {
        if (entry.getKey() == null || entry.getValue() == null) return;
        String propertyName = entry.getKey().toLowerCase(Locale.ROOT);
        if (RESERVED_PROPERTIES.contains(propertyName) || RESERVED_ENVELOPE_PROPERTIES.contains(propertyName)) {
            throw new MessageException("VALIDATION_FAILED: reserved RocketMQ user property: " + entry.getKey());
        }
        message.putUserProperty(entry.getKey(), entry.getValue());
    }
}
