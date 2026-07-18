package com.lab.message.contract;

@FunctionalInterface
public interface EventHandler<T> {

    void handle(EventEnvelope<T> event) throws Exception;
}
