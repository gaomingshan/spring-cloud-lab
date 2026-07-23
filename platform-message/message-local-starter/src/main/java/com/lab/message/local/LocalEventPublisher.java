package com.lab.message.local;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
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
    public void publish(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        applicationEventPublisher.publishEvent(new LocalMessageEvent(this, event));
    }

    @Override
    public void publish(String destination, BaseEvent event) {
        publish(event);
    }
}
