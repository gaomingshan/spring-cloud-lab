package com.lab.message.core;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface SchemaUpcaster {
    JsonNode upcast(JsonNode payload);
}
