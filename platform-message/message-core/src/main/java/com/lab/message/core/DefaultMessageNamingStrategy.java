package com.lab.message.core;

import com.lab.message.contract.MessageException;

public class DefaultMessageNamingStrategy implements MessageNamingStrategy {
    private static final String NAME_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    private final String destinationPrefix;
    private final String consumerGroupPrefix;

    public DefaultMessageNamingStrategy(MessageCoreProperties properties) {
        if (properties == null) {
            throw new MessageException("NAMING_FAILED: message core properties are required");
        }
        properties.validate();
        this.destinationPrefix = normalize(properties.getDestinationPrefix());
        this.consumerGroupPrefix = normalize(properties.getConsumerGroupPrefix());
    }

    @Override
    public String destination(String eventType) {
        return join(destinationPrefix, normalize(eventType));
    }

    @Override
    public String consumerGroup(String application, String purpose) {
        return join(consumerGroupPrefix, normalize(application), normalize(purpose));
    }

    private static String join(String... parts) {
        String result = String.join(".", parts);
        if (!result.matches("[a-z0-9]+(?:[.-][a-z0-9]+)*")) {
            throw new MessageException("NAMING_FAILED: normalized name is malformed");
        }
        return result;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) throw new MessageException("NAMING_FAILED: name is blank");
        String normalized = value.trim().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "").toLowerCase();
        if (!normalized.matches(NAME_PATTERN)) {
            throw new MessageException("NAMING_FAILED: name is punctuation-only or malformed");
        }
        return normalized;
    }
}
