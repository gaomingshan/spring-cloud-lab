package com.lab.reliable.message.publisher;

import org.springframework.scheduling.annotation.Scheduled;

public final class OutboxDispatchScheduler {
    private final OutboxDispatcher dispatcher;

    public OutboxDispatchScheduler(OutboxDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${reliable.message.outbox.poll-interval:PT1S}")
    public void dispatch() {
        dispatcher.dispatch();
    }
}
