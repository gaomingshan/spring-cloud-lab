package com.lab.message.rocketmq;

import com.lab.message.rocketmq.adapter.RocketMqMessageMapper;
import com.lab.message.rocketmq.adapter.RocketMqDestinationResolver;
import com.lab.message.rocketmq.adapter.RocketMqMessageFacade;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

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
    @ConditionalOnMissingBean(RocketMqMessageFacade.class)
    RocketMqMessageFacade rocketMqMessageFacade(
            RocketMQTemplate template,
            RocketMqMessageMapper mapper,
            RocketMqMessageProperties properties,
            RocketMQMessageListenerContainerRegistrar registrar,
            RocketMQProperties rocketMqProperties) {
        return new RocketMqMessageFacade(template, mapper, properties.getDelayLevels(), registrar, rocketMqProperties);
    }
}
