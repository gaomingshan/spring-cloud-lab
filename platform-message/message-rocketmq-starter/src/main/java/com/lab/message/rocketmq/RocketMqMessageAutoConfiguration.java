package com.lab.message.rocketmq;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.core.DefaultMessageNamingStrategy;
import com.lab.message.core.EventSerializer;
import com.lab.message.core.JsonEventSerializer;
import com.lab.message.core.MessageCoreProperties;
import com.lab.message.core.MessageNamingStrategy;
import com.lab.message.rocketmq.adapter.RocketMqConfiguration;
import com.lab.message.rocketmq.adapter.RocketMqDelayedProducer;
import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
import com.lab.message.rocketmq.adapter.RocketMqOrderedProducer;
import com.lab.message.rocketmq.adapter.RocketMqProducer;
import com.lab.message.rocketmq.adapter.RocketMqPublishResultMapper;
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
@ConditionalOnMissingBean(EventPublisher.class)
public class RocketMqMessageAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(EventSerializer.class)
    EventSerializer rocketMqEventSerializer(ObjectMapper objectMapper) {
        return new JsonEventSerializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(MessageNamingStrategy.class)
    MessageNamingStrategy rocketMqNamingStrategy(RocketMqMessageProperties properties) {
        properties.validate();
        MessageCoreProperties core = new MessageCoreProperties();
        core.setProducer(properties.getProducer().getGroup());
        core.setDestinationPrefix(properties.getNaming().getTopicPrefix());
        core.setConsumerGroupPrefix(properties.getNaming().getGroupPrefix());
        return new DefaultMessageNamingStrategy(core);
    }

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
    RocketMqMessageMapper rocketMqMessageMapper(EventSerializer serializer, MessageNamingStrategy namingStrategy) {
        return new RocketMqMessageMapper(serializer, namingStrategy);
    }

    @ConditionalOnMissingBean(TransactionMQProducer.class)
    @ConditionalOnBean(TransactionListener.class)
    @ConditionalOnProperty(prefix = "lab.message.rocketmq.transaction", name = "enabled", havingValue = "true")
    @Bean(destroyMethod = "shutdown")
    TransactionMQProducer rocketMqTransactionProducer(RocketMqConfiguration configuration,
                                                        ObjectProvider<TransactionListener> listener) throws Exception {
        return configuration.createTransactionProducer(listener.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqProducer.class)
    RocketMqProducer rocketMqProducer(RocketMqConfiguration configuration,
                                      RocketMqMessageMapper mapper,
                                      ObjectProvider<TransactionMQProducer> transactionProducer,
                                      RocketMqPublishResultMapper resultMapper) throws Exception {
        DefaultMQProducer producer = configuration.createProducer();
        TransactionMQProducer nativeTransactionProducer = transactionProducer.getIfAvailable();
        return new RocketMqProducer(producer, mapper, resultMapper, configuration,
                nativeTransactionProducer, true, false);
    }

    @Bean
    @ConditionalOnMissingBean
    RocketMqPublishResultMapper rocketMqPublishResultMapper() {
        return new RocketMqPublishResultMapper();
    }

    @Bean
    @ConditionalOnMissingBean(OrderedEventPublisher.class)
    OrderedEventPublisher rocketMqOrderedEventPublisher(RocketMqProducer producer) {
        return new RocketMqOrderedProducer(producer);
    }

    @Bean
    @ConditionalOnMissingBean(DelayedEventPublisher.class)
    DelayedEventPublisher rocketMqDelayedEventPublisher(RocketMqProducer producer,
                                                         RocketMqMessageProperties properties) {
        return properties.getDelayLevels().isEmpty() ? null : new RocketMqDelayedProducer(producer);
    }

    @Bean
    @ConditionalOnMissingBean(TransactionalEventPublisher.class)
    @ConditionalOnBean({TransactionListener.class, TransactionMQProducer.class})
    @ConditionalOnProperty(prefix = "lab.message.rocketmq.transaction", name = "enabled", havingValue = "true")
    TransactionalEventPublisher rocketMqTransactionalEventPublisher(RocketMqProducer producer,
                                                                      ObjectProvider<TransactionListener> listener) {
        return new RocketMqTransactionalProducer(producer, producer.transactionProducer());
    }
}
