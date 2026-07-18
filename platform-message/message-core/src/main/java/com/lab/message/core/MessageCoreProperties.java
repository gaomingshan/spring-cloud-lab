package com.lab.message.core;

public class MessageCoreProperties {
    private String producer = "unknown-producer";
    private int defaultSchemaVersion = 1;
    private String destinationPrefix = "lab";
    private String consumerGroupPrefix = "lab";

    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }
    public int getDefaultSchemaVersion() { return defaultSchemaVersion; }
    public void setDefaultSchemaVersion(int defaultSchemaVersion) { this.defaultSchemaVersion = defaultSchemaVersion; }
    public String getDestinationPrefix() { return destinationPrefix; }
    public void setDestinationPrefix(String destinationPrefix) { this.destinationPrefix = destinationPrefix; }
    public String getConsumerGroupPrefix() { return consumerGroupPrefix; }
    public void setConsumerGroupPrefix(String consumerGroupPrefix) { this.consumerGroupPrefix = consumerGroupPrefix; }
}
