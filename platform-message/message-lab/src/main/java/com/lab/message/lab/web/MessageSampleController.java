package com.lab.message.lab.web;

import com.lab.message.contract.EventPublisher;
import com.lab.message.lab.event.OrderCreatedEvent;
import com.lab.message.lab.support.SampleEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/sample", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageSampleController {
    private final EventPublisher eventPublisher;

    @PostMapping("/publish")
    public Map<String, String> publish() {
        OrderCreatedEvent event = SampleEvents.orderCreated();
        log.info("[rocketmq][publish] eventId={} orderId={} topic=lab_order_created_events thread={}",
                 event.getEventId(), event.getOrder().orderId(), Thread.currentThread().getName());
        eventPublisher.publish(event);
        return Map.of(
                "status", "published",
                "eventId", event.getEventId(),
                "orderId", event.getOrder().orderId());
    }
}
