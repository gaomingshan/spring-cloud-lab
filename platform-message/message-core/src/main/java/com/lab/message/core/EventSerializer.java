package com.lab.message.core;

import com.lab.message.contract.EventEnvelope;

public interface EventSerializer {
    byte[] serialize(EventEnvelope<?> event);
    <T> EventEnvelope<T> deserialize(byte[] bytes, Class<T> payloadType);
}
