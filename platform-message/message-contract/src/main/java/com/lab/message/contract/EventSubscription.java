package com.lab.message.contract;

/**
 * Internal/channel description assembled from external configuration.
 * Producer destinations are independent and must not be inferred from this type.
 */
public record EventSubscription(
        String destination,
        String consumerGroup,
        ConsumptionMode consumptionMode
) {
    public EventSubscription {
        if (destination == null || destination.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: subscription destination is required");
        }
        if (consumerGroup == null || consumerGroup.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: subscription consumerGroup is required");
        }
        consumptionMode = consumptionMode == null ? ConsumptionMode.CONCURRENT : consumptionMode;
    }
}
