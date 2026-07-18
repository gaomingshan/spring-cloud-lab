package com.lab.message.contract;

import java.time.Duration;
import java.util.Map;

public record PublishOptions(
        String destination,
        String key,
        Duration timeout,
        Map<String, String> headers
) {
    public PublishOptions {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}
