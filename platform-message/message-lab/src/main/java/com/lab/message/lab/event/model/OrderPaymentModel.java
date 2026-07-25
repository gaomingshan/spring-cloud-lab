package com.lab.message.lab.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lab.message.contract.MessageException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class OrderPaymentModel {
    private final String orderId;
    private final String paymentId;
    private final String channel;
    private final BigDecimal amount;

    @JsonCreator
    public OrderPaymentModel(@JsonProperty("orderId") String orderId,
                             @JsonProperty("paymentId") String paymentId,
                             @JsonProperty("channel") String channel,
                             @JsonProperty("amount") BigDecimal amount) {
        if (orderId == null || orderId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: orderId is required");
        }
        if (paymentId == null || paymentId.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: paymentId is required");
        }
        if (channel == null || channel.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: channel is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new MessageException("VALIDATION_FAILED: amount is invalid");
        }
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.channel = channel;
        this.amount = amount;
    }
}
