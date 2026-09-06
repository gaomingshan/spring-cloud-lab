package com.lab.message.contract;

/**
 * An {@link EventHandler} decorator that preserves the original handler metadata.
 * Message adapters must inspect {@link #delegate()} for annotations and generic type information,
 * while invoking this outer handler for runtime behavior.
 */
public interface DelegatingEventHandler<E extends BaseEvent> extends EventHandler<E> {
    EventHandler<E> delegate();
}
