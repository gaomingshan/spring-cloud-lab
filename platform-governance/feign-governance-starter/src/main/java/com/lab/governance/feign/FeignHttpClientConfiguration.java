package com.lab.governance.feign;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.clientconfig.HttpClient5FeignConfiguration.HttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(name = "org.apache.hc.client5.http.impl.classic.HttpClientBuilder")
@ConditionalOnProperty(prefix = "lab.governance.feign", name = "http-client5-enabled", havingValue = "true", matchIfMissing = true)
public class FeignHttpClientConfiguration {

    @Bean
    HttpClientBuilderCustomizer governanceHttpClientCustomizer(FeignProperties properties) {
        return builder -> builder
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(properties.getMaxConnections())
                        .setMaxConnPerRoute(properties.getMaxConnectionsPerRoute())
                        .setConnectionTimeToLive(TimeValue.of(properties.getConnectionTimeToLive()))
                        .build())
                .evictIdleConnections(TimeValue.of(properties.getIdleConnectionEvict()))
                .disableAutomaticRetries();
    }
}
