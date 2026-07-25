package com.lab.message.rocketmq.adapter.config;

import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.rocketmq.adapter.RocketMqConsumerRegistrar;
import com.lab.message.rocketmq.adapter.RocketMqDelayLevelResolver;
import com.lab.message.rocketmq.adapter.RocketMqEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqEventSubscriber;
import com.lab.message.rocketmq.adapter.RocketMqMessageFacade;
import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
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
@ConditionalOnProperty(prefix = "lab.message.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqAdapterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RocketMqMessageMapper.class)
    RocketMqMessageMapper rocketMqMessageMapper() {
        return new RocketMqMessageMapper();
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqDelayLevelResolver.class)
    RocketMqDelayLevelResolver rocketMqDelayLevelResolver(RocketMqAdapterProperties properties) {
        return new RocketMqDelayLevelResolver(properties.getDelayLevels());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(RocketMqMessageFacade.class)
    @ConditionalOnBean({RocketMQTemplate.class, RocketMQMessageListenerContainerRegistrar.class})
    RocketMqMessageFacade rocketMqMessageFacade(RocketMQTemplate template,
                                                RocketMqMessageMapper mapper,
                                                RocketMqDelayLevelResolver delayLevels,
                                                RocketMQMessageListenerContainerRegistrar registrar,
                                                RocketMqAdapterProperties adapterProperties) {
        RocketMqEventPublisher publisher = new RocketMqEventPublisher(template, mapper, delayLevels);
        RocketMqEventSubscriber subscriber = new RocketMqEventSubscriber(registrar, adapterProperties);
        return new RocketMqMessageFacade(publisher, subscriber);
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqConsumerRegistrar.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    RocketMqConsumerRegistrar rocketMqConsumerRegistrar(ApplicationContext applicationContext,
                                                        EventSubscriber eventSubscriber) {
        return new RocketMqConsumerRegistrar(applicationContext, eventSubscriber);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    EventPublisher eventPublisher(RocketMqMessageFacade facade) {
        return facade;
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    EventSubscriber eventSubscriber(RocketMqMessageFacade facade) {
        return facade;
    }
}
