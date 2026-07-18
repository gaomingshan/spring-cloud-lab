package com.lab.message.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.lab.message.contract.MessageException;

import java.util.HashMap;
import java.util.Map;

public class SchemaUpcasterRegistry {
    private final Map<Key, SchemaUpcaster> upcasters = new HashMap<>();

    public void register(String eventType, int fromVersion, SchemaUpcaster upcaster) {
        if (eventType == null || eventType.isBlank() || fromVersion <= 0 || upcaster == null) {
            throw new MessageException("UPCASTER_REGISTRATION_FAILED: invalid event type, version, or upcaster");
        }
        upcasters.put(new Key(eventType, fromVersion), upcaster);
    }

    public JsonNode upgrade(String eventType, int fromVersion, int currentVersion, JsonNode payload) {
        if (eventType == null || eventType.isBlank() || fromVersion <= 0 || currentVersion <= 0 || payload == null) {
            throw new MessageException("UPCAST_FAILED: invalid event type, version, or payload");
        }
        if (fromVersion > currentVersion) {
            throw new MessageException("UPCAST_FAILED: source schema is newer than requested schema");
        }
        JsonNode result = payload;
        for (int version = fromVersion; version < currentVersion; version++) {
            SchemaUpcaster upcaster = upcasters.get(new Key(eventType, version));
            if (upcaster == null) {
                throw new MessageException("UPCAST_FAILED: missing upcaster for " + eventType + " from version " + version);
            }
            result = upcaster.upcast(result);
            if (result == null) throw new MessageException("UPCAST_FAILED: upcaster returned null");
        }
        return result;
    }

    private record Key(String eventType, int fromVersion) { }
}
