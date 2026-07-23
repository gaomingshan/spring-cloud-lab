package com.lab.message.contract;

import java.time.Duration;

public interface DelayedEventPublisher extends EventPublisher {
    void publishDelayed(BaseEvent event, Duration delay);
}
