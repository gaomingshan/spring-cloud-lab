package com.lab.message.rocketmq;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqConfiguration;
import com.lab.message.rocketmq.adapter.RocketMqDelayedProducer;
import com.lab.message.rocketmq.adapter.RocketMqEventCodec;
import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
import com.lab.message.rocketmq.adapter.RocketMqOrderedProducer;
import com.lab.message.rocketmq.adapter.RocketMqEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqTransport;
import com.lab.message.rocketmq.adapter.RocketMqTransactionalProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(RocketMqMessageProperties.class)
@ConditionalOnClass(DefaultMQProducer.class)
@ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "true")
public class RocketMqMessageAutoConfiguration {
    @Bean
    RocketMqConfiguration rocketMqConfiguration(RocketMqMessageProperties properties) {
        properties.validate();
        RocketMqConfiguration configuration = new RocketMqConfiguration();
        configuration.setNameServer(properties.getNameServer());
        configuration.setProducerGroup(properties.getProducer().getGroup());
        configuration.setSendTimeoutMillis(properties.getProducer().getSendTimeout().toMillis() > Integer.MAX_VALUE
                ? Integer.MAX_VALUE : (int) properties.getProducer().getSendTimeout().toMillis());
        configuration.setRetryTimes(properties.getProducer().getRetryTimes());
        configuration.setRetryAnotherBroker(properties.getProducer().isRetryAnotherBroker());
        configuration.setDelayLevels(properties.getDelayLevels());
        return configuration;
    }

    @Bean
    RocketMqMessageMapper rocketMqMessageMapper(RocketMqEventCodec codec, RocketMqMessageProperties properties) {
        return new RocketMqMessageMapper(codec, properties.getNaming().getTopicPrefix());
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqEventCodec.class)
    RocketMqEventCodec rocketMqEventCodec(ObjectMapper objectMapper) {
        return new RocketMqEventCodec(objectMapper);
    }

    @ConditionalOnMissingBean(TransactionMQProducer.class)
    @ConditionalOnBean(TransactionListener.class)
    @ConditionalOnProperty(prefix = "lab.message.rocketmq.transaction", name = "enabled", havingValue = "true")
    @Bean(destroyMethod = "shutdown")
    TransactionMQProducer rocketMqTransactionProducer(RocketMqConfiguration configuration,
                                                        ObjectProvider<TransactionListener> listener) throws Exception {
        return configuration.createTransactionProducer(listener.getObject());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(RocketMqTransport.class)
    RocketMqTransport rocketMqTransport(RocketMqConfiguration configuration,
                                      RocketMqMessageMapper mapper,
                                      ObjectProvider<TransactionMQProducer> transactionProducer) throws Exception {
        DefaultMQProducer producer = configuration.createProducer();
        TransactionMQProducer nativeTransactionProducer = transactionProducer.getIfAvailable();
        return new RocketMqTransport(producer, mapper, configuration, nativeTransactionProducer, true);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    EventPublisher rocketMqEventPublisher(RocketMqTransport transport) {
        return new RocketMqEventPublisher(transport);
    }

    @Bean
    @ConditionalOnMissingBean(OrderedEventPublisher.class)
    OrderedEventPublisher rocketMqOrderedEventPublisher(RocketMqTransport transport) {
        return new RocketMqOrderedProducer(transport);
    }

    @Bean
    @ConditionalOnMissingBean(DelayedEventPublisher.class)
    @ConditionalOnDelayLevels
    DelayedEventPublisher rocketMqDelayedEventPublisher(RocketMqTransport transport) {
        return new RocketMqDelayedProducer(transport);
    }

    @Bean
    @ConditionalOnMissingBean(TransactionalEventPublisher.class)
    @ConditionalOnBean({TransactionListener.class, TransactionMQProducer.class})
    @ConditionalOnProperty(prefix = "lab.message.rocketmq.transaction", name = "enabled", havingValue = "true")
    TransactionalEventPublisher rocketMqTransactionalEventPublisher(RocketMqTransport transport) {
        return new RocketMqTransactionalProducer(transport);
    }
}
