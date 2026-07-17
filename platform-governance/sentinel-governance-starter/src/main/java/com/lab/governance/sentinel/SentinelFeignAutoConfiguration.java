package com.lab.governance.sentinel;

import feign.Feign;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@AutoConfiguration(after = SentinelAutoConfiguration.class)
@ConditionalOnClass(name = {"feign.Feign", "com.alibaba.csp.sentinel.SphU"})
@ConditionalOnProperty(prefix = "lab.governance.sentinel.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SentinelFeignAutoConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(Feign.Builder.class)
    Feign.Builder sentinelFeignBuilder() {
        return com.alibaba.cloud.sentinel.feign.SentinelFeign.builder();
    }
}
