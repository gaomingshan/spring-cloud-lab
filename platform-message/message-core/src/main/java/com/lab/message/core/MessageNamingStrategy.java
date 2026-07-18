package com.lab.message.core;

public interface MessageNamingStrategy {
    String destination(String eventType);
    String consumerGroup(String application, String purpose);
}
