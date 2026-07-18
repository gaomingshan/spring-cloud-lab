package com.lab.message.local;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.core.JsonEventSerializer;
import org.springframework.context.ApplicationEventPublisher;

public class LocalEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public LocalEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        if (applicationEventPublisher == null) {
            throw new MessageException("CONFIGURATION_FAILED: application event publisher is required");
        }
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(EventEnvelope<?> event) {
        JsonEventSerializer.validate(event);
        applicationEventPublisher.publishEvent(new LocalMessageEvent(this, event));
    }
}
