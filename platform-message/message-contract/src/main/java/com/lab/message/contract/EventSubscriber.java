package com.lab.message.contract;

public interface EventSubscriber {
    void subscribe(EventSubscription subscription, EventHandler handler);
}
