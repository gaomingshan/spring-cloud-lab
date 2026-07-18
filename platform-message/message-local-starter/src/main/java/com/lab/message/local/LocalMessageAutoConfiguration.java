package com.lab.message.local;

import com.lab.message.contract.EventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LocalMessageProperties.class)
@ConditionalOnProperty(prefix = "lab.message.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalMessageAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    LocalEventHandlerRegistry localEventHandlerRegistry() {
        return new LocalEventHandlerRegistry();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "false", matchIfMissing = true)
    LocalEventPublisher localEventPublisher(LocalMessageProperties properties,
                                             LocalEventHandlerRegistry registry) {
        return new LocalEventPublisher(properties, registry);
    }
}
