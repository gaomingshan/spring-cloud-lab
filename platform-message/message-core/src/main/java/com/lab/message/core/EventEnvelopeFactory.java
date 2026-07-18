package com.lab.message.core;

import com.lab.foundation.context.RequestContext;
import com.lab.foundation.context.RequestContextHolder;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class EventEnvelopeFactory {
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
        return new EventEnvelope<>(UUID.randomUUID().toString(), eventType,
                properties.getDefaultSchemaVersion(), properties.getProducer(),
                null, null, null, null, Instant.now(),
                headers.get("traceparent"), headers, payload);
    }

    private static void put(HashMap<String, String> headers, String name, String value) {
        if (value != null && !value.isBlank()) headers.put(name, value);
    }
}
