package com.lab.message.lab.web;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.lab.support.SampleEvents;
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
public class MessageSampleController {
    private final EventPublisher publisher;
    private final ObjectProvider<OrderedEventPublisher> orderedPublisher;
    private final ObjectProvider<DelayedEventPublisher> delayedPublisher;
    private final ObjectProvider<TransactionalEventPublisher> transactionalPublisher;

    public MessageSampleController(EventPublisher publisher,
                                   ObjectProvider<OrderedEventPublisher> orderedPublisher,
                                   ObjectProvider<DelayedEventPublisher> delayedPublisher,
                                   ObjectProvider<TransactionalEventPublisher> transactionalPublisher) {
        this.publisher = publisher;
        this.orderedPublisher = orderedPublisher;
        this.delayedPublisher = delayedPublisher;
        this.transactionalPublisher = transactionalPublisher;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("publisher", publisher.getClass().getName());
        body.put("ordered", orderedPublisher.getIfAvailable() != null);
        body.put("delayed", delayedPublisher.getIfAvailable() != null);
        body.put("transactional", transactionalPublisher.getIfAvailable() != null);
        return body;
    }

    @PostMapping("/publish")
    public Map<String, String> publish() {
        EventEnvelope<Map<String, Object>> event = SampleEvents.orderCreated();
        publisher.publish(event);
        return Map.of("mode", "publish", "eventId", event.eventId(), "eventType", event.eventType());
    }

    @PostMapping("/ordered")
    public Map<String, String> ordered() {
        OrderedEventPublisher capability = orderedPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("ordered publisher is not configured");
        }
        EventEnvelope<Map<String, Object>> event = SampleEvents.orderCreated("message-lab-order-key");
        capability.publishOrdered(event);
        return Map.of("mode", "ordered", "eventId", event.eventId(), "partitionKey", event.partitionKey());
    }

    @PostMapping("/delayed")
    public Map<String, String> delayed() {
        DelayedEventPublisher capability = delayedPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("delayed publisher is not configured (set lab.message.rocketmq.delay-levels)");
        }
        EventEnvelope<Map<String, Object>> event = SampleEvents.orderCreated();
        capability.publishDelayed(event, Duration.ofSeconds(10));
        return Map.of("mode", "delayed", "eventId", event.eventId(), "delay", "10s");
    }

    @PostMapping("/transactional")
    public Map<String, String> transactional() {
        TransactionalEventPublisher capability = transactionalPublisher.getIfAvailable();
        if (capability == null) {
            throw unavailable("transactional publisher is not configured (need RocketMQLocalTransactionListener bean)");
        }
        EventEnvelope<Map<String, Object>> event = SampleEvents.orderCreated();
        capability.publishInTransaction(event);
        return Map.of("mode", "transactional", "eventId", event.eventId());
    }

    private static MessageException unavailable(String reason) {
        return new MessageException("CAPABILITY_UNAVAILABLE: " + reason);
    }
}
