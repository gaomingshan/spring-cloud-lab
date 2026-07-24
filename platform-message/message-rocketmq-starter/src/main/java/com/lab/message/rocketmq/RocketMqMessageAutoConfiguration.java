package com.lab.message.rocketmq;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@AutoConfiguration
@EnableConfigurationProperties(RocketMqMessageProperties.class)
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "true")
public class RocketMqMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RocketMqDestinationResolver.class)
    RocketMqDestinationResolver rocketMqDestinationResolver(RocketMqMessageProperties properties) {
        properties.validate();
        String prefix = properties.getRocketmq().getNaming().getTopicPrefix();
        return (BaseEvent event) -> {
            String seed = event.getEventType();
            if (seed == null || seed.isBlank()) {
                seed = event.getClass().getSimpleName();
            }
            return prefix + seed.trim()
                    .replaceAll("([a-z])([A-Z])", "$1-$2")
                    .replaceAll("[^A-Za-z0-9_-]+", "-")
                    .toLowerCase(Locale.ROOT);
        };
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqMessageMapper.class)
    RocketMqMessageMapper rocketMqMessageMapper() {
        return new RocketMqMessageMapper();
    }

    @Bean
    @ConditionalOnMissingBean(RocketMqDelayLevelResolver.class)
    RocketMqDelayLevelResolver rocketMqDelayLevelResolver(RocketMqMessageProperties properties) {
        return new RocketMqDelayLevelResolver(properties.getRocketmq().getDelayLevels());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(RocketMqMessageFacade.class)
    @ConditionalOnBean({RocketMQTemplate.class, RocketMQMessageListenerContainerRegistrar.class, RocketMQProperties.class})
    RocketMqMessageFacade rocketMqMessageFacade(RocketMQTemplate template,
                                                RocketMqMessageMapper mapper,
                                                RocketMqDestinationResolver destinationResolver,
                                                RocketMqDelayLevelResolver delayLevels,
                                                RocketMQMessageListenerContainerRegistrar registrar,
                                                RocketMQProperties rocketMqProperties,
                                                RocketMqMessageProperties messageProperties) {
        messageProperties.validate();
        RocketMqEventPublisher publisher = new RocketMqEventPublisher(template, mapper, destinationResolver, delayLevels);
        Function<String, EventSubscription> lookup = bindingLookup(messageProperties);
        RocketMqEventSubscriber subscriber = new RocketMqEventSubscriber(registrar, rocketMqProperties, lookup);
        return new RocketMqMessageFacade(publisher, subscriber);
    }

    private static Function<String, EventSubscription> bindingLookup(RocketMqMessageProperties properties) {
        Map<String, EventSubscription> bindings = new LinkedHashMap<>();
        properties.getConsumers().forEach((name, binding) -> {
            if (binding == null || !binding.isEnabled()) {
                return;
            }
            bindings.put(name, new EventSubscription(
                    binding.getDestination(),
                    binding.getGroup(),
                    binding.getMode()));
        });
        return name -> {
            EventSubscription subscription = bindings.get(name);
            if (subscription == null) {
                throw new MessageException("CONFIGURATION_FAILED: consumer binding not found or disabled: " + name);
            }
            return subscription;
        };
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
