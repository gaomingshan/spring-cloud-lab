package com.lab.message.lab.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventDestination;
import com.lab.message.contract.MessageException;
import com.lab.message.lab.event.model.OrderCancelModel;
import com.lab.message.lab.event.model.OrderCreateModel;
import com.lab.message.lab.event.model.OrderPaymentModel;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@EventDestination("lab.order-events")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public final class OrderLifecycleEvent extends BaseEvent {
    private final OrderPhase phase;
    private final OrderCreateModel create;
    private final OrderPaymentModel payment;
    private final OrderCancelModel cancel;

    @JsonCreator
    public OrderLifecycleEvent(@JsonProperty("eventId") String eventId,
                               @JsonProperty("occurredAt") Instant occurredAt,
                               @JsonProperty("producer") String producer,
                               @JsonProperty("eventType") String eventType,
                               @JsonProperty("aggregateType") String aggregateType,
                               @JsonProperty("aggregateId") String aggregateId,
                               @JsonProperty("partitionKey") String partitionKey,
                               @JsonProperty("idempotencyKey") String idempotencyKey,
                               @JsonProperty("headers") Map<String, String> headers,
                               @JsonProperty("phase") OrderPhase phase,
                               @JsonProperty("create") OrderCreateModel create,
                               @JsonProperty("payment") OrderPaymentModel payment,
                               @JsonProperty("cancel") OrderCancelModel cancel) {
        super(eventId, occurredAt, producer, eventType, aggregateType, aggregateId, partitionKey, idempotencyKey, headers);
        if (phase == null) {
            throw new MessageException("VALIDATION_FAILED: phase is required");
        }
        this.phase = phase;
        this.create = create;
        this.payment = payment;
        this.cancel = cancel;
        validatePhasePayload();
    }

    private void validatePhasePayload() {
        switch (phase) {
            case CREATED -> {
                if (create == null) {
                    throw new MessageException("VALIDATION_FAILED: create model is required for CREATED");
                }
            }
            case PAID -> {
                if (payment == null) {
                    throw new MessageException("VALIDATION_FAILED: payment model is required for PAID");
                }
            }
            case CANCELLED -> {
                if (cancel == null) {
                    throw new MessageException("VALIDATION_FAILED: cancel model is required for CANCELLED");
                }
            }
        }
    }

    public static OrderLifecycleEvent created(String eventId, String producer, OrderCreateModel create) {
        return new OrderLifecycleEvent(
                eventId, Instant.now(), producer, "order.lifecycle.created",
                "Order", create.getOrderId(), create.getOrderId(), eventId,
                Map.of("sample", "true"),
                OrderPhase.CREATED, create, null, null);
    }

    public static OrderLifecycleEvent paid(String eventId, String producer, OrderPaymentModel payment) {
        return new OrderLifecycleEvent(
                eventId, Instant.now(), producer, "order.lifecycle.paid",
                "Order", payment.getOrderId(), payment.getOrderId(), eventId,
                Map.of("sample", "true"),
                OrderPhase.PAID, null, payment, null);
    }

    public static OrderLifecycleEvent cancelled(String eventId, String producer, OrderCancelModel cancel) {
        return new OrderLifecycleEvent(
                eventId, Instant.now(), producer, "order.lifecycle.cancelled",
                "Order", cancel.getOrderId(), cancel.getOrderId(), eventId,
                Map.of("sample", "true"),
                OrderPhase.CANCELLED, null, null, cancel);
    }
}
