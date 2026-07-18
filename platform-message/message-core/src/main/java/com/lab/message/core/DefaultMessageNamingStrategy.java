package com.lab.message.core;

import com.lab.message.contract.MessageException;

public class DefaultMessageNamingStrategy implements MessageNamingStrategy {
    private final MessageCoreProperties properties;

    public DefaultMessageNamingStrategy(MessageCoreProperties properties) {
        this.properties = properties;
    }

    @Override
    public String destination(String eventType) {
        return join(properties.getDestinationPrefix(), normalize(eventType));
    }

    @Override
    public String consumerGroup(String application, String purpose) {
        return join(properties.getConsumerGroupPrefix(), normalize(application), normalize(purpose));
    }

    private static String join(String... parts) {
        return String.join(".", parts);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) throw new MessageException("NAMING_FAILED: name is blank");
        return value.trim().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "").toLowerCase();
    }
}
