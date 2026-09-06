package com.lab.reliable.message.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.MessageException;

public final class JacksonEventCodec implements EventCodec {
    private final ObjectMapper objectMapper;

    public JacksonEventCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SerializedEvent encode(BaseEvent event) {
        try {
            return new SerializedEvent(event.getClass().getName(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new MessageException("SERIALIZATION_FAILED: " + event.getClass().getName(), e);
        }
    }

    @Override
    public BaseEvent decode(String eventType, String payload) {
        try {
            Class<?> type = Class.forName(eventType);
            if (!BaseEvent.class.isAssignableFrom(type)) {
                throw new MessageException("DESERIALIZE_FAILED: not an event type: " + eventType);
            }
            return objectMapper.readValue(payload, type.asSubclass(BaseEvent.class));
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("DESERIALIZE_FAILED: " + eventType, e);
        }
    }
}
