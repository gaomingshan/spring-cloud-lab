package com.lab.message.rocketmq;

import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqDelayedProducer;
import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
import com.lab.message.rocketmq.adapter.RocketMqDestinationResolver;
import com.lab.message.rocketmq.adapter.RocketMqOrderedProducer;
import com.lab.message.rocketmq.adapter.RocketMqEventPublisher;
import com.lab.message.rocketmq.adapter.RocketMqTransport;
import com.lab.message.rocketmq.adapter.RocketMqTransactionalProducer;
import com.lab.message.rocketmq.adapter.RocketMqEventSubscriber;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;

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
    RocketMqMessageMapper rocketMqMessageMapper(RocketMqDestinationResolver destinationResolver) {
        return new RocketMqMessageMapper(destinationResolver);
    }

    @Bean
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnMissingBean(RocketMqTransport.class)
    RocketMqTransport rocketMqTransport(RocketMQTemplate template, RocketMqMessageMapper mapper,
                                        RocketMqMessageProperties properties) {
        return new RocketMqTransport(template, mapper, properties.getDelayLevels());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnBean({RocketMQProperties.class, RocketMQMessageConverter.class})
    @ConditionalOnMissingBean(EventSubscriber.class)
    EventSubscriber rocketMqEventSubscriber(RocketMQProperties properties,
                                            RocketMQMessageConverter messageConverter,
                                            ApplicationContext applicationContext) {
        return new RocketMqEventSubscriber(properties, messageConverter, applicationContext);
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
    @ConditionalOnBean({RocketMQTemplate.class, RocketMQLocalTransactionListener.class})
    TransactionalEventPublisher rocketMqTransactionalEventPublisher(RocketMqTransport transport) {
        return new RocketMqTransactionalProducer(transport);
    }
}
