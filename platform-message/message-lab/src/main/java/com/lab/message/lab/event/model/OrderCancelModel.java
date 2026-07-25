package com.lab.message.lab.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lab.message.contract.MessageException;
import lombok.Getter;

@Getter
public final class OrderCancelModel {
    private final String orderId;
    private final String reason;

    @JsonCreator
    public OrderCancelModel(@JsonProperty("orderId") String orderId,
                            @JsonProperty("reason") String reason) {
        if (orderId == null || orderId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: orderId is required");
        }
        if (reason == null || reason.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: reason is required");
        }
        this.orderId = orderId;
        this.reason = reason;
    }
}
