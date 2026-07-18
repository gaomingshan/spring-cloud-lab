package com.lab.message.core;

import com.lab.foundation.context.RequestContext;
import com.lab.foundation.context.RequestContextHolder;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;

import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.UUID;
import java.security.SecureRandom;

public class EventEnvelopeFactory {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final MessageCoreProperties properties;

    public EventEnvelopeFactory(MessageCoreProperties properties) {
        if (properties == null) {
            throw new MessageException("CONFIGURATION_FAILED: message core properties are required");
        }
        properties.validate();
        this.properties = properties;
    }

    public <T> EventEnvelope<T> create(String eventType, T payload) {
        var headers = new HashMap<String, String>();
        RequestContext context = RequestContextHolder.get();
        if (context != null) {
            put(headers, "request-id", context.requestId());
            put(headers, "trace-id", context.traceId());
            put(headers, "span-id", context.spanId());
            put(headers, "tenant-id", context.tenantId());
            put(headers, "principal-id", context.principalId());
        }
        String traceparent = traceparent(context);
        headers.put("traceparent", traceparent);
        return new EventEnvelope<>(UUID.randomUUID().toString(), eventType,
                properties.getDefaultSchemaVersion(), properties.getProducer(),
                null, null, null, null, Instant.now(),
                traceparent, headers, payload);
    }

    private static String traceparent(RequestContext context) {
        String traceId = context == null ? null : hex(context.traceId(), 32);
        String spanId = context == null ? null : hex(context.spanId(), 16);
        if (traceId == null) traceId = randomHex(16);
        if (spanId == null) spanId = randomHex(8);
        return "00-" + traceId + "-" + spanId + "-01";
    }

    private static String hex(String value, int length) {
        if (value == null) return null;
        String normalized = value.replace("-", "").toLowerCase();
        return normalized.matches("[0-9a-f]{" + length + "}")
                && !normalized.chars().allMatch(character -> character == '0') ? normalized : null;
    }

    private static String randomHex(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }

    private static void put(HashMap<String, String> headers, String name, String value) {
        if (value != null && !value.isBlank()) headers.put(name, value);
    }
}
