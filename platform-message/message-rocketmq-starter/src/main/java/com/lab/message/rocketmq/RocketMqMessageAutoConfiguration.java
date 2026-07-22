package com.lab.message.rocketmq;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqDelayLevelResolver;
import com.lab.message.rocketmq.adapter.RocketMqDestinationResolver;
import com.lab.message.rocketmq.adapter.RocketMqEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqEventSubscriber;
import com.lab.message.rocketmq.adapter.RocketMqMessageFacade;
import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@EnableConfigurationProperties(RocketMqMessageProperties.class)
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "true")
public class RocketMqMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RocketMqDestinationResolver.class)
    RocketMqDestinationResolver rocketMqDestinationResolver(RocketMqMessageProperties properties) {
        properties.validate();
        String prefix = properties.getNaming().getTopicPrefix();
        return event -> prefix + event.eventType().trim().replaceAll("[^A-Za-z0-9_-]+", "-");
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqMessageMapper.class)
    RocketMqMessageMapper rocketMqMessageMapper(RocketMqDestinationResolver destinationResolver) {
        return new RocketMqMessageMapper(destinationResolver);
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqDelayLevelResolver.class)
    RocketMqDelayLevelResolver rocketMqDelayLevelResolver(RocketMqMessageProperties properties) {
        return new RocketMqDelayLevelResolver(properties.getDelayLevels());
    }

    /**
     * Single facade bean implements all contracts. Collaborators are not registered as beans
     * so interface injection always resolves to this facade without ambiguity.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(RocketMqMessageFacade.class)
    @ConditionalOnBean({RocketMQTemplate.class, RocketMQMessageListenerContainerRegistrar.class, RocketMQProperties.class})
    RocketMqMessageFacade rocketMqMessageFacade(RocketMQTemplate template,
                                                RocketMqMessageMapper mapper,
                                                RocketMqDelayLevelResolver delayLevels,
                                                RocketMQMessageListenerContainerRegistrar registrar,
                                                RocketMQProperties rocketMqProperties) {
        RocketMqEventPublisher publisher = new RocketMqEventPublisher(template, mapper, delayLevels);
        RocketMqEventSubscriber subscriber = new RocketMqEventSubscriber(registrar, rocketMqProperties);
        return new RocketMqMessageFacade(publisher, subscriber);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    EventPublisher eventPublisher(RocketMqMessageFacade facade) {
        return facade;
    }

    @Bean
    @ConditionalOnMissingBean(OrderedEventPublisher.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    OrderedEventPublisher orderedEventPublisher(RocketMqMessageFacade facade) {
        return facade;
    }

    @Bean
    @ConditionalOnMissingBean(DelayedEventPublisher.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    @ConditionalOnDelayLevels
    DelayedEventPublisher delayedEventPublisher(RocketMqMessageFacade facade) {
        return facade;
    }

    @Bean
    @ConditionalOnMissingBean(TransactionalEventPublisher.class)
    @ConditionalOnBean({RocketMqMessageFacade.class, RocketMQLocalTransactionListener.class})
    TransactionalEventPublisher transactionalEventPublisher(RocketMqMessageFacade facade) {
        return facade;
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    @ConditionalOnBean(RocketMqMessageFacade.class)
    EventSubscriber eventSubscriber(RocketMqMessageFacade facade) {
        return facade;
    }
}
