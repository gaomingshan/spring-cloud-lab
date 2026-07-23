package com.lab.message.contract;

public interface EventSubscriber {
    /**
     * Bind a concrete event type to a subscription. {@code eventType} must be a concrete class
     * (not {@link BaseEvent} and not abstract).
     */
    <E extends BaseEvent> void subscribe(EventSubscription subscription,
                                         Class<E> eventType,
                                         EventHandler<E> handler);
}
