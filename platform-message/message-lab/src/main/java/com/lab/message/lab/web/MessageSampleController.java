package com.lab.message.lab.web;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.lab.support.SampleEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/sample", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageSampleController {
    private final EventPublisher publisher;
    private final ObjectProvider<OrderedEventPublisher> orderedPublisher;
    private final ObjectProvider<DelayedEventPublisher> delayedPublisher;
    private final ObjectProvider<TransactionalEventPublisher> transactionalPublisher;

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("publisher", publisher.getClass().getName());
        body.put("ordered", orderedPublisher.getIfAvailable() != null);
        body.put("delayed", delayedPublisher.getIfAvailable() != null);
        body.put("transactional", transactionalPublisher.getIfAvailable() != null);
        body.put("eventType", OrderLifecycleEvent.class.getName());
        return body;
    }

    @PostMapping("/publish")
    public Map<String, String> publish() {
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        publisher.publish(event);
        return result("publish", event);
    }

    @PostMapping("/publish/paid")
    public Map<String, String> publishPaid() {
        OrderLifecycleEvent event = SampleEvents.orderPaid();
        publisher.publish(event);
        return result("publish-paid", event);
    }

    @PostMapping("/publish/cancelled")
    public Map<String, String> publishCancelled() {
        OrderLifecycleEvent event = SampleEvents.orderCancelled();
        publisher.publish(event);
        return result("publish-cancelled", event);
    }

    @PostMapping("/ordered")
    public Map<String, String> ordered() {
        OrderedEventPublisher capability = orderedPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("ordered publisher is not configured");
        }
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        capability.publishOrdered(event);
        return result("ordered", event);
    }

    @PostMapping("/delayed")
    public Map<String, String> delayed() {
        DelayedEventPublisher capability = delayedPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("delayed publisher is not configured (set lab.message.adapter.delay-levels)");
        }
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        capability.publishDelayed(event, Duration.ofSeconds(10));
        return result("delayed", event);
    }

    @PostMapping("/transactional")
    public Map<String, String> transactional() {
        TransactionalEventPublisher capability = transactionalPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("transactional publisher is not configured (need RocketMQLocalTransactionListener bean)");
        }
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        capability.publishInTransaction(event);
        return result("transactional", event);
    }

    private static Map<String, String> result(String mode, OrderLifecycleEvent event) {
        return Map.of(
                "mode", mode,
                "eventId", event.getEventId(),
                "phase", event.getPhase().name(),
                "eventType", event.getEventType() == null ? "" : event.getEventType(),
                "aggregateId", event.getAggregateId() == null ? "" : event.getAggregateId());
    }

    private static MessageException unavailable(String reason) {
        return new MessageException("CAPABILITY_UNAVAILABLE: " + reason);
    }
}
