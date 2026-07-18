package com.lab.message.contract;

import java.time.Duration;

public interface DelayedEventPublisher extends EventPublisher {

    void publishDelayed(EventEnvelope<?> event, Duration delay);
}
