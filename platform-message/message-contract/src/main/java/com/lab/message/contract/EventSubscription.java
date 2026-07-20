package com.lab.message.contract;

public record EventSubscription(
        String destination,
        String consumerGroup,
        String selector,
        ConsumptionMode consumptionMode
) {
    public EventSubscription {
        if (destination == null || destination.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: subscription destination is required");
        }
        if (consumerGroup == null || consumerGroup.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: subscription consumerGroup is required");
        }
        selector = selector == null || selector.isBlank() ? "*" : selector;
        consumptionMode = consumptionMode == null ? ConsumptionMode.CONCURRENT : consumptionMode;
    }
}
