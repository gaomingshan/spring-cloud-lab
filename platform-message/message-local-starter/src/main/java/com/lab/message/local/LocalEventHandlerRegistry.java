package com.lab.message.local;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalEventHandlerRegistry {
    private final Map<String, CopyOnWriteArrayList<EventHandler<?>>> handlers = new ConcurrentHashMap<>();

    public <T> void register(String eventType, EventHandler<T> handler) {
        if (eventType == null || eventType.isBlank() || handler == null) {
            throw new MessageException("HANDLER_REGISTRATION_FAILED: event type and handler are required");
        }
        handlers.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public List<EventHandler<?>> handlersFor(String eventType) {
        return List.copyOf(handlers.getOrDefault(eventType, new CopyOnWriteArrayList<>()));
    }

    @SuppressWarnings("unchecked")
    public void dispatch(EventEnvelope<?> event) throws Exception {
        Exception firstFailure = null;
        for (EventHandler<?> handler : handlersFor(event.eventType())) {
            try {
                ((EventHandler<Object>) handler).handle((EventEnvelope<Object>) event);
            } catch (Exception failure) {
                if (firstFailure == null) firstFailure = failure;
                else firstFailure.addSuppressed(failure);
            }
        }
        if (firstFailure != null) throw firstFailure;
    }
}
