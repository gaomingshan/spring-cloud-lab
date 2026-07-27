package com.lab.message.local;

import com.lab.message.contract.EventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "lab.message.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalMessageAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    @ConditionalOnProperty(prefix = "lab.message.rocketmq.adapter", name = "enabled", havingValue = "false", matchIfMissing = true)
    LocalEventPublisher localEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new LocalEventPublisher(applicationEventPublisher);
    }
}
