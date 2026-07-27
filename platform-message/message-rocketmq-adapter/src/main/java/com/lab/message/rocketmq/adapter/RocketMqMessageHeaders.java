package com.lab.message.rocketmq.adapter;

public final class RocketMqMessageHeaders {
    public static final String EVENT_ID = "lab.event-id";
    public static final String EVENT_CLASS = "lab.event-class";
    public static final String EVENT_TYPE = "lab.event-type";
    public static final String PRODUCER = "lab.producer";
    public static final String AGGREGATE_TYPE = "lab.aggregate-type";
    public static final String AGGREGATE_ID = "lab.aggregate-id";

    private RocketMqMessageHeaders() {
    }
}
