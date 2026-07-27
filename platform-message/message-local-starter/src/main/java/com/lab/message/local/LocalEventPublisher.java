package com.lab.message.local;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class LocalEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(BaseEvent event) {
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is null");
        }
        applicationEventPublisher.publishEvent(new LocalMessageEvent(this, event));
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        publish(event);
    }

}
