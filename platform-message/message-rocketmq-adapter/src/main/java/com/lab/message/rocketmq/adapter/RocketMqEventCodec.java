package com.lab.message.rocketmq.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;

public final class RocketMqEventCodec {
    private final ObjectMapper objectMapper;

    public RocketMqEventCodec(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new MessageException("CONFIGURATION_FAILED: object mapper is required");
        }
        this.objectMapper = objectMapper.copy().registerModule(new JavaTimeModule());
    }

    public byte[] encode(EventEnvelope<?> event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new MessageException("SERIALIZATION_FAILED: could not encode RocketMQ message", e);
        }
    }
}
