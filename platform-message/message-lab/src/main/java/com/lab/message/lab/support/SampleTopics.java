package com.lab.message.lab.support;

public final class SampleTopics {
    /** Shared domain topic for order lifecycle concrete event. */
    public static final String ORDER_EVENTS = "lab.order-events";

    public static final String GROUP_PRIMARY = "message-lab-order-primary";
    public static final String GROUP_AUDIT = "message-lab-order-audit";

    private SampleTopics() {
    }
}
