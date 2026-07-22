package com.lab.message.lab.support;

/**
 * Sample naming must stay aligned with RocketMQ destination resolution:
 * topic = topic-prefix + eventType sanitized (non [A-Za-z0-9_-] -> '-').
 * Default prefix is "lab." so order.created.v1 -> lab.order-created-v1.
 */
public final class SampleTopics {
    public static final String EVENT_TYPE_ORDER_CREATED = "order.created.v1";
    public static final String TOPIC_ORDER_CREATED = "lab.order-created-v1";

    public static final String GROUP_PRIMARY = "message-lab-order-primary";
    public static final String GROUP_AUDIT = "message-lab-order-audit";

    private SampleTopics() {
    }
}
