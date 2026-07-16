package com.lab.governance.gateway;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "governanceRequestIdFilter")
    GlobalFilter governanceRequestIdFilter(GatewayProperties properties) {
        return (exchange, chain) -> {
            var requestId = exchange.getRequest().getHeaders().getFirst(properties.getRequestIdHeader());
            var value = requestId == null || requestId.isBlank() ? java.util.UUID.randomUUID().toString() : requestId;
            var request = exchange.getRequest().mutate().header(properties.getRequestIdHeader(), value).build();
            var response = exchange.getResponse();
            response.getHeaders().set(properties.getRequestIdHeader(), value);
            return chain.filter(exchange.mutate().request(request).build()).then(Mono.empty());
        };
    }
}
