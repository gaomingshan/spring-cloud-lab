package com.lab.message.core;

import com.lab.message.contract.MessageException;

public class MessageCoreProperties {
    private String producer;
    private int defaultSchemaVersion = 1;
    private String destinationPrefix = "lab";
    private String consumerGroupPrefix = "lab";

    public String getProducer() { return producer; }
    public void setProducer(String producer) {
        if (producer == null || producer.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: producer is required");
        }
        this.producer = producer;
    }
    public int getDefaultSchemaVersion() { return defaultSchemaVersion; }
    public void setDefaultSchemaVersion(int defaultSchemaVersion) {
        if (defaultSchemaVersion <= 0) {
            throw new MessageException("CONFIGURATION_FAILED: defaultSchemaVersion must be positive");
        }
        this.defaultSchemaVersion = defaultSchemaVersion;
    }
    public String getDestinationPrefix() { return destinationPrefix; }
    public void setDestinationPrefix(String destinationPrefix) { this.destinationPrefix = destinationPrefix; }
    public String getConsumerGroupPrefix() { return consumerGroupPrefix; }
    public void setConsumerGroupPrefix(String consumerGroupPrefix) { this.consumerGroupPrefix = consumerGroupPrefix; }

    void validate() {
        if (producer == null || producer.isBlank()) {
            throw new MessageException("CONFIGURATION_FAILED: producer is required");
        }
        if (defaultSchemaVersion <= 0) {
            throw new MessageException("CONFIGURATION_FAILED: defaultSchemaVersion must be positive");
        }
    }
}
