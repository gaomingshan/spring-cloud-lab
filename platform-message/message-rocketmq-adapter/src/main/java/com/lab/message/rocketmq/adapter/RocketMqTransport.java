package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;

import java.time.Duration;

public final class RocketMqTransport {
    private final DefaultMQProducer producer;
    private final RocketMqMessageMapper mapper;
    private final RocketMqConfiguration configuration;
    private final org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer;
    private final boolean ownsProducer;

    public RocketMqTransport(DefaultMQProducer producer, RocketMqMessageMapper mapper,
                             RocketMqConfiguration configuration,
                             org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer,
                             boolean ownsProducer) {
        if (producer == null || mapper == null || configuration == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ transport dependencies are required");
        }
        this.producer = producer;
        this.mapper = mapper;
        this.configuration = configuration;
        this.transactionProducer = transactionProducer;
        this.ownsProducer = ownsProducer;
    }

    public void send(EventEnvelope<?> event) {
        try {
            ensureSuccess(producer.send(mapper.map(event), configuration.getSendTimeoutMillis()), "ordinary");
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_SEND_FAILED: ordinary message send failed", e);
        }
    }

    public void sendOrdered(EventEnvelope<?> event) {
        if (event == null || RocketMqMessageMapper.blank(event.partitionKey())) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
        try {
            Message message = mapper.map(event);
            MessageQueueSelector selector = (queues, ignored, argument) ->
                    queues.get(Math.floorMod(argument.toString().hashCode(), queues.size()));
            ensureSuccess(producer.send(message, selector, event.partitionKey(), configuration.getSendTimeoutMillis()), "ordered");
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_ORDERED_SEND_FAILED: ordered message send failed", e);
        }
    }

    public void sendDelayed(EventEnvelope<?> event, Duration delay) {
        Integer delayLevel = delay == null ? null : configuration.getDelayLevels().get(delay);
        if (delayLevel == null) {
            throw new MessageException("VALIDATION_FAILED: delay is not explicitly configured");
        }
        try {
            ensureSuccess(producer.send(mapper.map(event, delayLevel), configuration.getSendTimeoutMillis()), "delayed");
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_DELAYED_SEND_FAILED: delayed message send failed", e);
        }
    }

    public void sendInTransaction(EventEnvelope<?> event) {
        if (transactionProducer == null || transactionProducer.getTransactionListener() == null) {
            throw new MessageException("CONFIGURATION_FAILED: transaction producer and listener are required");
        }
        try {
            var result = transactionProducer.sendMessageInTransaction(mapper.map(event), event);
            if (result == null || result.getSendStatus() != org.apache.rocketmq.client.producer.SendStatus.SEND_OK) {
                throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction submission was not accepted");
            }
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction message send failed", e);
        }
    }

    public void close() {
        if (ownsProducer) producer.shutdown();
    }

    private static void ensureSuccess(org.apache.rocketmq.client.producer.SendResult result, String mode) {
        if (result == null || result.getSendStatus() != org.apache.rocketmq.client.producer.SendStatus.SEND_OK) {
            String status = result == null || result.getSendStatus() == null
                    ? "no result" : result.getSendStatus().name();
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED: " + status);
        }
    }
}
