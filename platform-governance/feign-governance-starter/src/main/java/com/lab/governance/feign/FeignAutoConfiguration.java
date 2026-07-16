package com.lab.governance.feign;

import com.lab.foundation.context.RequestContextHolder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Request;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
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
    Logger.Level governanceFeignLoggerLevel(FeignProperties properties) {
        return properties.getLoggerLevel();
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
                if (context.tenantId() != null) template.header("X-Tenant-Id", context.tenantId());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    Request.Options governanceFeignOptions(FeignProperties properties) {
        return new Request.Options(properties.getConnectTimeout(), properties.getReadTimeout(), true);
    }

    @Bean
    @ConditionalOnMissingBean
    Retryer governanceFeignRetryer() { return Retryer.NEVER_RETRY; }

    @Bean
    @ConditionalOnMissingBean
    ErrorDecoder governanceFeignErrorDecoder() { return new ErrorDecoder.Default(); }

    @Bean
    @ConditionalOnMissingBean
    Encoder governanceFeignEncoder(ObjectFactory<HttpMessageConverters> converters) {
        return new SpringEncoder(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    Decoder governanceFeignDecoder(ObjectFactory<HttpMessageConverters> converters) {
        return new SpringDecoder(converters);
    }
}
