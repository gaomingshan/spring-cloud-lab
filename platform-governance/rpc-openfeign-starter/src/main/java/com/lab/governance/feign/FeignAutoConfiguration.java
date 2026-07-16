package com.lab.governance.feign;

import com.lab.foundation.context.RequestContextHolder;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FeignProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Logger.Level governanceFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    @ConditionalOnMissingBean(name = "governanceFeignRequestInterceptor")
    RequestInterceptor governanceFeignRequestInterceptor(FeignProperties properties) {
        return template -> {
            var context = RequestContextHolder.get();
            if (context != null) {
                if (properties.isRequestIdPropagation()) template.header("X-Request-Id", context.requestId());
                if (properties.isTraceContextPropagation()) template.header("X-Trace-Id", context.traceId());
                if (context.spanId() != null) template.header("X-Span-Id", context.spanId());
                if (properties.isAuthorizationPropagation() && context.principalId() != null) {
                    template.header("X-Principal-Id", context.principalId());
                }
            }
        };
    }
}
