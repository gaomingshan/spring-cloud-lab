package com.lab.message.lab.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lab.message.contract.MessageException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public final class OrderCreateModel {
    private final String orderId;
    private final List<String> skuIds;
    private final BigDecimal amount;

    @JsonCreator
    public OrderCreateModel(@JsonProperty("orderId") String orderId,
                            @JsonProperty("skuIds") List<String> skuIds,
                            @JsonProperty("amount") BigDecimal amount) {
        if (orderId == null || orderId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: orderId is required");
        }
        if (skuIds == null || skuIds.isEmpty()) {
            throw new MessageException("VALIDATION_FAILED: skuIds is required");
        }
        if (amount == null || amount.signum() < 0) {
            throw new MessageException("VALIDATION_FAILED: amount is invalid");
        }
        this.orderId = orderId;
        this.skuIds = List.copyOf(skuIds);
        this.amount = amount;
    }
}
