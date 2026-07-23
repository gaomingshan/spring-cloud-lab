package com.lab.message.contract;

@FunctionalInterface
public interface EventHandler<E extends BaseEvent> {
    void handle(E event);
}
