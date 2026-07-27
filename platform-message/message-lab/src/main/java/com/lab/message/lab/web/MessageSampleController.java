package com.lab.message.lab.web;

import com.lab.message.contract.EventPublisher;
import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.lab.support.SampleEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/sample", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageSampleController {
    private final EventPublisher publisher;

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("publisher", publisher.getClass().getName());
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
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        publisher.publishOrdered(event);
        return result("ordered", event);
    }

    @PostMapping("/transactional")
    public Map<String, String> transactional() {
        OrderLifecycleEvent event = SampleEvents.orderCreated();
        publisher.publishInTransaction(event);
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
}
