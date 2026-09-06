package com.lab.reliable.message.publisher;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.reliable.message.codec.EventCodec;
import com.lab.reliable.message.config.ReliableMessageProperties;
import com.lab.reliable.message.model.DeliveryMode;
import com.lab.reliable.message.model.OutboxMessage;
import com.lab.reliable.message.store.ReliableMessageStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final EventPublisher publisher;
    private final ReliableMessageStore store;
    private final EventCodec codec;
    private final ReliableMessageProperties properties;
    private final Clock clock;

    public OutboxDispatcher(EventPublisher publisher, ReliableMessageStore store, EventCodec codec,
                            ReliableMessageProperties properties, Clock clock) {
        this.publisher = publisher;
        this.store = store;
        this.codec = codec;
        this.properties = properties;
        this.clock = clock;
    }

    public void dispatch() {
        Instant now = clock.instant();
        store.recoverExpiredOutbox(now);
        for (OutboxMessage message : store.claimOutbox(properties.getOutbox().getBatchSize(), now,
                now.plus(properties.getOutbox().getLeaseTimeout()))) {
            deliver(message);
        }
    }

    private void deliver(OutboxMessage message) {
        try {
            BaseEvent event = codec.decode(message.eventType(), message.payload());
            if (message.deliveryMode() == DeliveryMode.ORDERED) {
                publisher.publishOrdered(event);
            } else {
                publisher.publish(event);
            }
            store.markOutboxSent(message.messageId(), clock.instant());
        } catch (Exception e) {
            if (message.attemptCount() >= properties.getRetry().getMaxAttempts()) {
                store.markOutboxDead(message.messageId(), errorMessage(e));
                log.error("Reliable message {} exhausted retries", message.messageId(), e);
                return;
            }
            store.markOutboxRetry(message.messageId(), nextAttempt(message.attemptCount()), errorMessage(e));
            log.warn("Reliable message {} delivery failed", message.messageId(), e);
        }
    }

    private Instant nextAttempt(int attemptCount) {
        Duration initial = properties.getRetry().getInitialDelay();
        Duration maximum = properties.getRetry().getMaxDelay();
        long multiplier = 1L << Math.min(attemptCount, 30);
        Duration delay = initial.multipliedBy(multiplier);
        return clock.instant().plus(delay.compareTo(maximum) > 0 ? maximum : delay);
    }

    private static String errorMessage(Exception error) {
        String message = error.getMessage();
        return message == null ? error.getClass().getName() : message;
    }
}
