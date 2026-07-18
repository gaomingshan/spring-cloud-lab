package com.lab.message.core;

import com.lab.message.contract.MessageException;

public class MessageCoreProperties {
    private String producer;
    private String destinationPrefix = "lab";
    private String consumerGroupPrefix = "lab";

    public String getProducer() { return producer; }
    public void setProducer(String producer) {
        if (producer == null || producer.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: producer is required");
        }
        this.producer = producer;
    }
    public String getDestinationPrefix() { return destinationPrefix; }
    public void setDestinationPrefix(String destinationPrefix) { this.destinationPrefix = destinationPrefix; }
    public String getConsumerGroupPrefix() { return consumerGroupPrefix; }
    public void setConsumerGroupPrefix(String consumerGroupPrefix) { this.consumerGroupPrefix = consumerGroupPrefix; }

    void validate() {
        if (producer == null || producer.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: producer is required");
        }
        validatePrefix(destinationPrefix, "destinationPrefix");
        validatePrefix(consumerGroupPrefix, "consumerGroupPrefix");
    }

    private static void validatePrefix(String prefix, String name) {
        if (prefix == null || prefix.isBlank() || !prefix.trim().matches("[A-Za-z0-9]+(?:[-.][A-Za-z0-9]+)*")) {
            throw new MessageException("CONFIGURATION_FAILED: " + name + " is malformed");
        }
    }
}
