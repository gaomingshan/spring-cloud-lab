package com.lab.message.local;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;
import com.lab.message.contract.PublishStatus;
import com.lab.message.core.JsonEventSerializer;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class LocalEventPublisher implements EventPublisher, AutoCloseable {
    private final LocalMessageProperties properties;
    private final LocalEventHandlerRegistry registry;
    private final ThreadPoolExecutor executor;
    private final boolean ownsExecutor;

    public LocalEventPublisher(LocalMessageProperties properties, LocalEventHandlerRegistry registry) {
        if (properties == null || registry == null) {
            throw new MessageException("CONFIGURATION_FAILED: local publisher dependencies are required");
        }
        properties.validate();
        this.properties = properties;
        this.registry = registry;
        this.executor = new ThreadPoolExecutor(properties.getExecutor().getCoreSize(), properties.getExecutor().getMaxSize(),
                60L, java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(properties.getExecutor().getQueueCapacity()),
                new ThreadPoolExecutor.AbortPolicy());
        this.ownsExecutor = true;
    }

    public LocalEventPublisher(LocalMessageProperties properties, LocalEventHandlerRegistry registry,
                               ThreadPoolExecutor executor) {
        if (properties == null || registry == null || executor == null) {
            throw new MessageException("CONFIGURATION_FAILED: local publisher dependencies are required");
        }
        properties.validate();
        this.properties = properties;
        this.registry = registry;
        this.executor = executor;
        this.ownsExecutor = false;
    }

    public LocalEventHandlerRegistry registry() { return registry; }

    @Override
    public PublishResult publish(EventEnvelope<?> event) {
        return publish(event, new PublishOptions(null, null, null, java.util.Map.of()));
    }

    @Override
    public PublishResult publish(EventEnvelope<?> event, PublishOptions options) {
        JsonEventSerializer.validate(event);
        if ("async".equalsIgnoreCase(properties.getDispatchMode())) {
            try {
                executor.execute(() -> dispatch(event));
                return new PublishResult(event.eventId(), PublishStatus.ACCEPTED, localId(), null);
            } catch (RejectedExecutionException e) {
                return failed(event, "LOCAL_DISPATCH_REJECTED: executor queue is full", e);
            }
        }
        try {
            registry.dispatch(event);
            return new PublishResult(event.eventId(), PublishStatus.SENT, localId(), null);
        } catch (Exception e) {
            return failed(event, "LOCAL_DISPATCH_FAILED: handler threw an exception", e);
        }
    }

    private void dispatch(EventEnvelope<?> event) {
        try {
            registry.dispatch(event);
        } catch (Exception ignored) {
            // Async publication is accepted before handler execution; failures cannot change that result.
        }
    }

    private static PublishResult failed(EventEnvelope<?> event, String reason, Exception cause) {
        String detail = cause.getMessage();
        return new PublishResult(event.eventId(), PublishStatus.FAILED,
                null, detail == null || detail.isBlank() ? reason : reason + ": " + detail);
    }

    private static String localId() { return "local-" + UUID.randomUUID(); }

    @Override
    public void close() {
        if (ownsExecutor) executor.shutdown();
    }
}
