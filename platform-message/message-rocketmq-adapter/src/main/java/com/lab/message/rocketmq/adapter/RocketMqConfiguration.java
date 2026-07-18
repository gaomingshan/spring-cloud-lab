package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.MessageException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RocketMqConfiguration {
    private static final Pattern NAME_SERVER = Pattern.compile(
            "(?i)(?:[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?|\\[[0-9a-f:]+\\]):[1-9][0-9]{0,4}(?:,(?:[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?|\\[[0-9a-f:]+\\]):[1-9][0-9]{0,4})*");
    private String nameServer;
    private String producerGroup = "lab-message-producer";
    private int sendTimeoutMillis = 3000;
    private int retryTimes = 2;
    private boolean retryAnotherBroker;
    private final Map<Duration, Integer> delayLevels = new LinkedHashMap<>();

    public String getNameServer() { return nameServer; }
    public void setNameServer(String nameServer) { this.nameServer = nameServer; }
    public String getProducerGroup() { return producerGroup; }
    public void setProducerGroup(String producerGroup) { this.producerGroup = producerGroup; }
    public int getSendTimeoutMillis() { return sendTimeoutMillis; }
    public void setSendTimeoutMillis(int sendTimeoutMillis) { this.sendTimeoutMillis = sendTimeoutMillis; }
    public int getRetryTimes() { return retryTimes; }
    public void setRetryTimes(int retryTimes) { this.retryTimes = retryTimes; }
    public boolean isRetryAnotherBroker() { return retryAnotherBroker; }
    public void setRetryAnotherBroker(boolean retryAnotherBroker) { this.retryAnotherBroker = retryAnotherBroker; }
    public Map<Duration, Integer> getDelayLevels() { return Map.copyOf(delayLevels); }
    public void setDelayLevels(Map<Duration, Integer> levels) {
        delayLevels.clear();
        if (levels != null) delayLevels.putAll(levels);
    }

    public DefaultMQProducer createProducer() throws MQClientException {
        validate();
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        configure(producer);
        try {
            producer.start();
            return producer;
        } catch (Exception e) {
            producer.shutdown();
            throw new MessageException("CONFIGURATION_FAILED: could not start RocketMQ producer", e);
        }
    }

    public TransactionMQProducer createTransactionProducer(TransactionListener listener) throws MQClientException {
        if (listener == null) throw new MessageException("CONFIGURATION_FAILED: transaction listener is required");
        validate();
        TransactionMQProducer producer = new TransactionMQProducer(producerGroup);
        configure(producer);
        producer.setTransactionListener(listener);
        try {
            producer.start();
            return producer;
        } catch (Exception e) {
            producer.shutdown();
            throw new MessageException("CONFIGURATION_FAILED: could not start RocketMQ transaction producer", e);
        }
    }

    private void configure(DefaultMQProducer producer) {
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(sendTimeoutMillis);
        producer.setRetryTimesWhenSendFailed(retryTimes);
        producer.setRetryAnotherBrokerWhenNotStoreOK(retryAnotherBroker);
    }

    private void validate() {
        if (nameServer == null || nameServer.isBlank() || !NAME_SERVER.matcher(nameServer.trim()).matches()) {
            throw new MessageException("CONFIGURATION_FAILED: nameServer is invalid");
        }
        if (producerGroup == null || producerGroup.isBlank()) throw new MessageException("CONFIGURATION_FAILED: producerGroup is required");
        if (sendTimeoutMillis <= 0 || retryTimes < 0) throw new MessageException("CONFIGURATION_FAILED: producer settings are invalid");
        for (Map.Entry<Duration, Integer> entry : delayLevels.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isNegative() || entry.getKey().isZero()
                    || entry.getValue() == null || entry.getValue() <= 0) {
                throw new MessageException("CONFIGURATION_FAILED: delay levels are invalid");
            }
        }
    }
}
