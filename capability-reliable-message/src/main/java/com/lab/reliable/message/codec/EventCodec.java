package com.lab.reliable.message.codec;

import com.lab.message.contract.BaseEvent;

public interface EventCodec {
    SerializedEvent encode(BaseEvent event);

    BaseEvent decode(String eventType, String payload);
}
