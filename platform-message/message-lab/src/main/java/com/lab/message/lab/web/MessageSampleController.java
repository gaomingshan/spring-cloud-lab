package com.lab.message.lab.web;

import com.lab.message.contract.EventPublisher;
import com.lab.message.lab.event.OrderCreatedEvent;
import com.lab.message.lab.support.SampleEvents;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/sample", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageSampleController {
    private final EventPublisher eventPublisher;

    @PostMapping("/publish")
    @Transactional
    public Map<String, String> publish() {
        OrderCreatedEvent event = SampleEvents.orderCreated();
        log.info("[message][publish] mode=ordinary eventId={} orderId={} topic=lab_order_created_events thread={}",
                event.getEventId(), event.getOrder().orderId(), Thread.currentThread().getName());
        eventPublisher.publish(event);
        return result("published", event);
    }

    @PostMapping("/publish/ordered")
    @Transactional
    public Map<String, String> publishOrdered() {
        OrderCreatedEvent event = SampleEvents.orderCreated();
        log.info("[message][publish] mode=ordered eventId={} partitionKey={} topic=lab_order_created_events thread={}",
                event.getEventId(), event.getPartitionKey(), Thread.currentThread().getName());
        eventPublisher.publishOrdered(event);
        return result("published-ordered", event);
    }

    private static Map<String, String> result(String status, OrderCreatedEvent event) {
        return Map.of(
                "status", status,
                "eventId", event.getEventId(),
                "orderId", event.getOrder().orderId(),
                "partitionKey", event.getPartitionKey());
    }
}
