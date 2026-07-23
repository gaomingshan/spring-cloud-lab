package com.lab.message.local;

import com.lab.message.contract.BaseEvent;
import org.springframework.context.ApplicationEvent;

public final class LocalMessageEvent extends ApplicationEvent {
    private final BaseEvent event;

    public LocalMessageEvent(Object source, BaseEvent event) {
        super(source);
        this.event = event;
    }

    public BaseEvent event() {
        return event;
    }
}
