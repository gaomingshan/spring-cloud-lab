package com.lab.message.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;

import java.io.IOException;

public class JsonEventSerializer implements EventSerializer {
    private final ObjectMapper objectMapper;

    public JsonEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().findAndRegisterModules();
    }

    @Override
    public byte[] serialize(EventEnvelope<?> event) {
        validate(event);
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (IOException e) {
            throw new MessageException("SERIALIZATION_FAILED: could not serialize event", e);
        }
    }

    @Override
    public <T> EventEnvelope<T> deserialize(byte[] bytes, Class<T> payloadType) {
        if (bytes == null || bytes.length == 0) {
            throw new MessageException("DESERIALIZATION_FAILED: event bytes are empty");
        }
        if (payloadType == null) {
            throw new MessageException("DESERIALIZATION_FAILED: payload type is null");
        }
        try {
            JsonNode node = objectMapper.readTree(bytes);
            java.util.Map<String, String> headers = node.has("headers")
                    ? objectMapper.convertValue(node.get("headers"), objectMapper.getTypeFactory()
                    .constructMapType(java.util.Map.class, String.class, String.class))
                    : java.util.Map.of();
            EventEnvelope<T> event = new EventEnvelope<>(
                    text(node, "eventId"), text(node, "eventType"),
                    node.path("schemaVersion").asInt(0), text(node, "producer"),
                    text(node, "aggregateType"), text(node, "aggregateId"),
                    text(node, "partitionKey"), text(node, "idempotencyKey"),
                    objectMapper.treeToValue(node.get("occurredAt"), java.time.Instant.class),
                    text(node, "traceparent"), headers,
                    objectMapper.treeToValue(node.get("payload"), payloadType));
            validate(event);
            return event;
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("DESERIALIZATION_FAILED: invalid event JSON", e);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    static void validate(EventEnvelope<?> event) {
        if (event == null) throw new MessageException("VALIDATION_FAILED: event is null");
        if (blank(event.eventId())) throw new MessageException("VALIDATION_FAILED: eventId is blank");
        if (blank(event.eventType())) throw new MessageException("VALIDATION_FAILED: eventType is blank");
        if (event.schemaVersion() <= 0) throw new MessageException("VALIDATION_FAILED: schemaVersion must be positive");
        if (blank(event.producer())) throw new MessageException("VALIDATION_FAILED: producer is blank");
        if (event.payload() == null) throw new MessageException("VALIDATION_FAILED: payload is null");
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
