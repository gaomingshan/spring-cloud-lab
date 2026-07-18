package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.PublishOptions;
import com.lab.message.contract.PublishResult;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.Map;

public class RocketMqProducer implements EventPublisher, AutoCloseable {
    private final DefaultMQProducer producer;
    private final RocketMqMessageMapper messageMapper;
    private final RocketMqPublishResultMapper resultMapper;
    private final RocketMqConfiguration configuration;
    private final org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer;
    private final boolean ownsProducer;
    private final boolean ownsTransactionProducer;

    public RocketMqProducer(DefaultMQProducer producer, RocketMqMessageMapper messageMapper,
                            RocketMqPublishResultMapper resultMapper, RocketMqConfiguration configuration) {
        this(producer, messageMapper, resultMapper, configuration, null, false, false);
    }

    public RocketMqProducer(DefaultMQProducer producer, RocketMqMessageMapper messageMapper,
                            RocketMqPublishResultMapper resultMapper, RocketMqConfiguration configuration,
                            org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer) {
        this(producer, messageMapper, resultMapper, configuration, transactionProducer, false, false);
    }

    public RocketMqProducer(DefaultMQProducer producer, RocketMqMessageMapper messageMapper,
                            RocketMqPublishResultMapper resultMapper, RocketMqConfiguration configuration,
                            org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer,
                            boolean ownsProducer, boolean ownsTransactionProducer) {
        if (producer == null || messageMapper == null || resultMapper == null || configuration == null) {
            throw new MessageException("CONFIGURATION_FAILED: producer dependencies are required");
        }
        this.producer = producer;
        this.messageMapper = messageMapper;
        this.resultMapper = resultMapper;
        this.configuration = configuration;
        this.transactionProducer = transactionProducer;
        this.ownsProducer = ownsProducer;
        this.ownsTransactionProducer = ownsTransactionProducer;
    }

    public PublishResult publish(EventEnvelope<?> event) { return publish(event, new PublishOptions(null, null, null, Map.of())); }

    public PublishResult publish(EventEnvelope<?> event, PublishOptions options) {
        try {
            SendResult result = producer.send(messageMapper.map(event, options), timeout(options));
            return resultMapper.sent(event, result);
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            return resultMapper.failed(event, "ROCKETMQ_SEND_FAILED", e);
        }
    }

    PublishResult publishOrdered(EventEnvelope<?> event, String partitionKey) {
        if (RocketMqMessageMapper.blank(partitionKey)) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required");
        }
        try {
            Message message = messageMapper.map(event, new PublishOptions(null, partitionKey, null, Map.of()));
            SendResult result = producer.send(message, (queues, ignored, argument) ->
                    queues.get(Math.floorMod(argument.toString().hashCode(), queues.size())), partitionKey, timeout(null));
            return resultMapper.sent(event, result);
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            return resultMapper.failed(event, "ROCKETMQ_ORDERED_SEND_FAILED", e);
        }
    }

    PublishResult publishDelayed(EventEnvelope<?> event, java.time.Duration delay) {
        Integer level = delay == null ? null : configuration.getDelayLevels().get(delay);
        if (level == null) throw new MessageException("VALIDATION_FAILED: delay is not explicitly configured");
        try {
            SendResult result = producer.send(messageMapper.map(event, null, level), configuration.getSendTimeoutMillis());
            return resultMapper.sent(event, result);
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            return resultMapper.failed(event, "ROCKETMQ_DELAYED_SEND_FAILED", e);
        }
    }

    PublishResult publishTransactional(EventEnvelope<?> event) {
        if (transactionProducer == null || transactionProducer.getTransactionListener() == null) {
            throw new MessageException("CONFIGURATION_FAILED: transaction producer and listener are required");
        }
        try {
            SendResult result = transactionProducer.sendMessageInTransaction(messageMapper.map(event, null), event);
            return resultMapper.accepted(event, result);
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            return resultMapper.failed(event, "ROCKETMQ_TRANSACTION_FAILED", e);
        }
    }

    public org.apache.rocketmq.client.producer.TransactionMQProducer transactionProducer() {
        return transactionProducer;
    }

    private int timeout(PublishOptions options) {
        if (options == null || options.timeout() == null) return configuration.getSendTimeoutMillis();
        long millis = options.timeout().toMillis();
        if (millis <= 0 || millis > Integer.MAX_VALUE) throw new MessageException("VALIDATION_FAILED: timeout is invalid");
        return (int) millis;
    }

    @Override public void close() {
        if (ownsProducer) producer.shutdown();
        if (ownsTransactionProducer) transactionProducer.shutdown();
    }
}
