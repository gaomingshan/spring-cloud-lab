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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ConditionalOnClass({KafkaTemplate.class, ConcurrentKafkaListenerContainerFactory.class})
@Profile("kafka")
public class KafkaAdapterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper kafkaObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnBean(KafkaTemplate.class)
    EventPublisher eventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new KafkaEventPublisher(kafkaTemplate, objectMapper, new KafkaPartitionResolver(kafkaTemplate));
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    @ConditionalOnBean(ConcurrentKafkaListenerContainerFactory.class)
    EventSubscriber eventSubscriber(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory,
            ObjectMapper objectMapper) {
        return new KafkaEventSubscriber(containerFactory, objectMapper);
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
