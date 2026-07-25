package com.lab.message.local;

import com.lab.message.contract.BaseEvent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public final class LocalMessageEvent extends ApplicationEvent {
    private final BaseEvent event;

    public LocalMessageEvent(Object source, BaseEvent event) {
        super(source);
        this.event = event;
    }

    /** Compatibility alias used by lab sample. */
    public BaseEvent event() {
        return event;
    }
}
