package com.lab.governance.gateway;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import com.lab.foundation.context.RequestContext;
import com.lab.foundation.context.RequestContextHolder;
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
            var headers = exchange.getRequest().getHeaders();
            RequestContextHolder.set(new RequestContext(value, headers.getFirst("X-Trace-Id"),
                    headers.getFirst("X-Span-Id"), headers.getFirst("X-Tenant-Id"), headers.getFirst("X-Principal-Id")));
            var request = exchange.getRequest().mutate().header(properties.getRequestIdHeader(), value).build();
            var response = exchange.getResponse();
            response.getHeaders().set(properties.getRequestIdHeader(), value);
            return chain.filter(exchange.mutate().request(request).build())
                    .doFinally(signal -> RequestContextHolder.clear());
        };
    }
}
