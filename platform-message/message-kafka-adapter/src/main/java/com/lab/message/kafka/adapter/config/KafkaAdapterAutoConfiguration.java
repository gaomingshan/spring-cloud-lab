package com.lab.message.kafka.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.kafka.adapter.publisher.KafkaEventPublisher;
import com.lab.message.kafka.adapter.publisher.KafkaPartitionResolver;
import com.lab.message.kafka.adapter.subscriber.KafkaConsumerRegistrar;
import com.lab.message.kafka.adapter.subscriber.KafkaEventSubscriber;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@EnableConfigurationProperties(KafkaAdapterProperties.class)
@ConditionalOnClass({KafkaTemplate.class, ConcurrentKafkaListenerContainerFactory.class})
@ConditionalOnProperty(prefix = "lab.message.kafka.adapter", name = "enabled", havingValue = "true")
public class KafkaAdapterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper kafkaObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnBean(KafkaTemplate.class)
    EventPublisher eventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new KafkaEventPublisher(kafkaTemplate, objectMapper, new KafkaPartitionResolver(kafkaTemplate));
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(EventSubscriber.class)
    @ConditionalOnBean(ConcurrentKafkaListenerContainerFactory.class)
    EventSubscriber eventSubscriber(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory,
            KafkaAdapterProperties adapterProperties,
            ObjectMapper objectMapper) {
        return new KafkaEventSubscriber(containerFactory, adapterProperties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(KafkaConsumerRegistrar.class)
    @ConditionalOnBean(EventSubscriber.class)
    KafkaConsumerRegistrar kafkaConsumerRegistrar(
            ApplicationContext applicationContext,
            EventSubscriber eventSubscriber) {
        return new KafkaConsumerRegistrar(applicationContext, eventSubscriber);
    }
}
