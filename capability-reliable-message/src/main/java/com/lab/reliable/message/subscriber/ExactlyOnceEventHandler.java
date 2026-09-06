package com.lab.reliable.message.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.DelegatingEventHandler;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import com.lab.reliable.message.config.ReliableMessageProperties;
import com.lab.reliable.message.model.ProcessingClaim;
import com.lab.reliable.message.store.ReliableMessageStore;
import java.time.Clock;
import java.time.Instant;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

final class ExactlyOnceEventHandler<E extends BaseEvent> implements DelegatingEventHandler<E> {
    private final EventHandler<E> delegate;
    private final String consumerScope;
    private final ReliableMessageStore store;
    private final ReliableMessageProperties properties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    ExactlyOnceEventHandler(EventHandler<E> delegate, String consumerScope, ReliableMessageStore store,
                            ReliableMessageProperties properties, Clock clock,
                            PlatformTransactionManager transactionManager) {
        this.delegate = delegate;
        this.consumerScope = consumerScope;
        this.store = store;
        this.properties = properties;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public EventHandler<E> delegate() {
        return delegate;
    }

    @Override
    public void handle(E event) {
        String fingerprint = requireFingerprint(event);
        Instant now = clock.instant();
        ProcessingClaim claim = store.claimProcessing(consumerScope, fingerprint, event.getEventType(), now,
                now.plus(properties.getProcessing().getLeaseTimeout()));
        if (claim == ProcessingClaim.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == ProcessingClaim.IN_PROGRESS) {
            throw new MessageException("PROCESSING_IN_PROGRESS: " + consumerScope + ":" + fingerprint);
        }
        try {
            transactionTemplate.executeWithoutResult(status -> {
                delegate.handle(event);
                store.markProcessingSucceeded(consumerScope, fingerprint, clock.instant());
            });
        } catch (RuntimeException e) {
            store.markProcessingFailed(consumerScope, fingerprint, errorMessage(e), clock.instant());
            throw e;
        }
    }

    private static String requireFingerprint(BaseEvent event) {
        if (event == null || event.getEventId() == null || event.getEventId().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: eventId is required for exactly-once processing");
        }
        return event.getEventId();
    }

    private static String errorMessage(RuntimeException error) {
        String message = error.getMessage();
        return message == null ? error.getClass().getName() : message;
    }
}
