package com.lab.message.lab;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.PublishResult;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.core.EventEnvelopeFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageProbeController {
    private final EventPublisher publisher;
    private final EventEnvelopeFactory envelopeFactory;
    private final ObjectProvider<OrderedEventPublisher> orderedPublisher;
    private final ObjectProvider<DelayedEventPublisher> delayedPublisher;
    private final ObjectProvider<TransactionalEventPublisher> transactionalPublisher;

    public MessageProbeController(EventPublisher publisher, EventEnvelopeFactory envelopeFactory,
                                  ObjectProvider<OrderedEventPublisher> orderedPublisher,
                                  ObjectProvider<DelayedEventPublisher> delayedPublisher,
                                  ObjectProvider<TransactionalEventPublisher> transactionalPublisher) {
        this.publisher = publisher;
        this.envelopeFactory = envelopeFactory;
        this.orderedPublisher = orderedPublisher;
        this.delayedPublisher = delayedPublisher;
        this.transactionalPublisher = transactionalPublisher;
    }

    @GetMapping("/probe")
    public PublishResult probe() {
        return publisher.publish(event());
    }

    @PostMapping("/publish")
    public PublishResult publish() {
        return publisher.publish(event());
    }

    @PostMapping("/ordered")
    public PublishResult ordered() {
        OrderedEventPublisher capability = orderedPublisher.getIfAvailable();
        return capability == null
                ? unsupported("ordered publisher is not configured")
                : capability.publishOrdered(event(), "message-lab-order");
    }

    @PostMapping("/delayed")
    public PublishResult delayed() {
        DelayedEventPublisher capability = delayedPublisher.getIfAvailable();
        return capability == null
                ? unsupported("delayed publisher is not configured")
                : capability.publishDelayed(event(), Duration.ofSeconds(10));
    }

    @PostMapping("/transactional")
    public PublishResult transactional() {
        TransactionalEventPublisher capability = transactionalPublisher.getIfAvailable();
        return capability == null
                ? unsupported("transactional publisher is not configured")
                : capability.publishTransactional(event());
    }

    private EventEnvelope<Map<String, Object>> event() {
        return envelopeFactory.create("lab.message.probe.v1", Map.of(
                "source", "message-lab",
                "timestamp", System.currentTimeMillis()));
    }

    private static PublishResult unsupported(String reason) {
        return new PublishResult(null, com.lab.message.contract.PublishStatus.FAILED, null,
                "CAPABILITY_UNAVAILABLE: " + reason);
    }
}
