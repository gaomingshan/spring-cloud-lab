package com.lab.message.contract;

import java.time.Duration;

public interface DelayedEventPublisher extends EventPublisher {

    PublishResult publishDelayed(EventEnvelope<?> event, Duration delay);
}
