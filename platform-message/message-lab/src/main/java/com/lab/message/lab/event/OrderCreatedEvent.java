package com.lab.message.lab.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventProducer;
import com.lab.message.contract.MessageException;
import com.lab.message.lab.event.model.OrderCreateModel;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@EventProducer(topic = "lab.order-created-events")
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OrderCreatedEvent extends BaseEvent {
    private final OrderCreateModel order;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("producer") String producer,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("aggregateType") String aggregateType,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("partitionKey") String partitionKey,
            @JsonProperty("idempotencyKey") String idempotencyKey,
            @JsonProperty("headers") Map<String, String> headers,
            @JsonProperty("order") OrderCreateModel order) {
        super(eventId, occurredAt, producer, eventType, aggregateType, aggregateId,
                partitionKey, idempotencyKey, headers);
        if (order == null) {
            throw new MessageException("VALIDATION_FAILED: order is required");
        }
        this.order = order;
    }

    public static OrderCreatedEvent sample(String eventId, String producer, OrderCreateModel order) {
        return new OrderCreatedEvent(
                eventId,
                Instant.now(),
                producer,
                "order.created",
                "Order",
                order.getOrderId(),
                order.getOrderId(),
                eventId,
                Map.of("sample", "true"),
                order);
    }
}
