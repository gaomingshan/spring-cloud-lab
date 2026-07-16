package com.lab.governance.sentinel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(SentinelProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.sentinel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SentinelAutoConfiguration {
}
