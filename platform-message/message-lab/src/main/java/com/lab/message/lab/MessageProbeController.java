package com.lab.message.lab;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.TransactionalEventPublisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageProbeController {
    private final EventPublisher publisher;
    private final ObjectProvider<OrderedEventPublisher> orderedPublisher;
    private final ObjectProvider<DelayedEventPublisher> delayedPublisher;
    private final ObjectProvider<TransactionalEventPublisher> transactionalPublisher;

    public MessageProbeController(EventPublisher publisher,
                                  ObjectProvider<OrderedEventPublisher> orderedPublisher,
                                  ObjectProvider<DelayedEventPublisher> delayedPublisher,
                                  ObjectProvider<TransactionalEventPublisher> transactionalPublisher) {
        this.publisher = publisher;
        this.orderedPublisher = orderedPublisher;
        this.delayedPublisher = delayedPublisher;
        this.transactionalPublisher = transactionalPublisher;
    }

    @GetMapping("/probe")
    public void probe() {
        publisher.publish(event());
    }

    @PostMapping("/publish")
    public void publish() {
        publisher.publish(event());
    }

    @PostMapping("/ordered")
    public void ordered() {
        OrderedEventPublisher capability = orderedPublisher.getIfAvailable();
        if (capability == null) throw unsupported("ordered publisher is not configured");
        EventEnvelope<Map<String, Object>> event = event("message-lab-order");
        capability.publishOrdered(event);
    }

    @PostMapping("/delayed")
    public void delayed() {
        DelayedEventPublisher capability = delayedPublisher.getIfAvailable();
        if (capability == null) throw unsupported("delayed publisher is not configured");
        capability.publishDelayed(event(), Duration.ofSeconds(10));
    }

    @PostMapping("/transactional")
    public void transactional() {
        TransactionalEventPublisher capability = transactionalPublisher.getIfAvailable();
        if (capability == null) throw unsupported("transactional publisher is not configured");
        capability.publishInTransaction(event());
    }

    private EventEnvelope<Map<String, Object>> event() {
        return event(null);
    }

    private EventEnvelope<Map<String, Object>> event(String partitionKey) {
        return new EventEnvelope<>(UUID.randomUUID().toString(), "lab.message.probe.v1", "message-lab",
                null, null, partitionKey, null, Instant.now(), null, Map.of(), Map.of(
                "source", "message-lab",
                "timestamp", System.currentTimeMillis()));
    }

    private static MessageException unsupported(String reason) {
        return new MessageException("CAPABILITY_UNAVAILABLE: " + reason);
    }
}
