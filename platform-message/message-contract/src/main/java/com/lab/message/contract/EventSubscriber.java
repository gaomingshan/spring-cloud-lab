package com.lab.message.contract;

public interface EventSubscriber {
    /**
     * Bind a concrete event type to an externally configured consumer binding.
     * Channel settings come from configuration under {@code bindingName}, not from event annotations.
     */
    <E extends BaseEvent> void bind(String bindingName, Class<E> eventType, EventHandler<E> handler);
}
