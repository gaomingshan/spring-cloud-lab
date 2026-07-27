package com.lab.message.rocketmq.adapter.config;

import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.rocketmq.adapter.publisher.RocketMqEventPublisher;
import com.lab.message.rocketmq.adapter.subscriber.RocketMqConsumerRegistrar;
import com.lab.message.rocketmq.adapter.subscriber.RocketMqEventSubscriber;
import com.lab.message.rocketmq.adapter.support.RocketMqMessageMapper;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
@EnableConfigurationProperties(RocketMqAdapterProperties.class)
@ConditionalOnClass({RocketMQTemplate.class, RocketMQAutoConfiguration.class})
@ConditionalOnProperty(prefix = "lab.message.rocketmq.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqAdapterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RocketMqMessageMapper.class)
    RocketMqMessageMapper rocketMqMessageMapper() {
        return new RocketMqMessageMapper();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    EventPublisher eventPublisher(RocketMQTemplate template, RocketMqMessageMapper mapper) {
        return new RocketMqEventPublisher(template, mapper);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(EventSubscriber.class)
    @ConditionalOnBean(RocketMQMessageListenerContainerRegistrar.class)
    EventSubscriber eventSubscriber(
        RocketMQMessageListenerContainerRegistrar registrar,
        RocketMqAdapterProperties adapterProperties
    ) {
        return RocketMqEventSubscriber.create(registrar, adapterProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqConsumerRegistrar.class)
    @ConditionalOnBean(EventSubscriber.class)
    RocketMqConsumerRegistrar rocketMqConsumerRegistrar(
        ApplicationContext applicationContext,
        EventSubscriber eventSubscriber
    ) {
        return new RocketMqConsumerRegistrar(applicationContext, eventSubscriber);
    }
}
