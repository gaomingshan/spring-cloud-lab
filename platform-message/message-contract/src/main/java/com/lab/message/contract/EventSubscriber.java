package com.lab.message.contract;

/**
 * Registers {@link EventHandler} consumers. Channel identity comes from
 * {@link EventConsumer} on the handler implementation class.
 */
public interface EventSubscriber {
    /**
     * Register a handler. The handler's concrete class must carry {@link EventConsumer}
     * (topic + group). Event type is taken from the handler's type argument.
     */
    <E extends BaseEvent> void bind(EventHandler<E> handler);
}
