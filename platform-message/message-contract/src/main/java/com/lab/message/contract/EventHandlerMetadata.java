package com.lab.message.contract;

public final class EventHandlerMetadata {
    private EventHandlerMetadata() {
    }

    public static EventHandler<?> unwrap(EventHandler<?> handler) {
        EventHandler<?> current = handler;
        while (current instanceof DelegatingEventHandler<?> delegating) {
            current = delegating.delegate();
        }
        return current;
    }
}
