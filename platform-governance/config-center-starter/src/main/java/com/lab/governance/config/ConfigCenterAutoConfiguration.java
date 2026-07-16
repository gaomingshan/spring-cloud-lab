package com.lab.governance.config;

import com.lab.governance.contract.DynamicConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ConfigCenterProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.config", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConfigCenterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    DynamicConfigService dynamicConfigService(ObjectMapper objectMapper) {
        return new NacosDynamicConfigService(objectMapper);
    }
}
