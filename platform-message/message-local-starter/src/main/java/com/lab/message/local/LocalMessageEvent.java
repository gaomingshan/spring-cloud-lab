package com.lab.message.local;

import com.lab.message.contract.EventEnvelope;
import org.springframework.context.ApplicationEvent;

public final class LocalMessageEvent extends ApplicationEvent {
    private final EventEnvelope<?> envelope;

    public LocalMessageEvent(Object source, EventEnvelope<?> envelope) {
        super(source);
        this.envelope = envelope;
    }

    public EventEnvelope<?> envelope() {
        return envelope;
    }
}
