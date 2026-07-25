package com.lab.message.contract;

/**
 * Optional manual registration. Primary path is automatic discovery of
 * {@link EventConsumer}-annotated consumer methods.
 */
public interface EventSubscriber {
    <E extends BaseEvent> void bind(Class<E> eventType, String topic, String group, EventHandler<E> handler);
}
