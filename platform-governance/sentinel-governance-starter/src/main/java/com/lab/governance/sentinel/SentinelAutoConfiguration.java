package com.lab.governance.sentinel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import feign.Capability;
import feign.Feign;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(SentinelProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.sentinel", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({Feign.class, com.alibaba.csp.sentinel.SphU.class})
public class SentinelAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "lab.governance.sentinel.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    Capability sentinelFeignCapability(SentinelProperties properties) {
        return new SentinelFeignCapability(properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "lab.governance.sentinel.nacos", name = "enabled", havingValue = "true")
    NacosDataSource<List<FlowRule>> sentinelFlowDataSource(SentinelProperties properties, ObjectMapper mapper) {
        var nacos = properties.getNacos();
        var source = new NacosDataSource<List<FlowRule>>(nacos.getServerAddr(), nacos.getGroup(),
                nacos.getFlowDataId(), converter(mapper, new TypeReference<>() {}));
        FlowRuleManager.register2Property(source.getProperty());
        return source;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "lab.governance.sentinel.nacos", name = "enabled", havingValue = "true")
    NacosDataSource<List<DegradeRule>> sentinelDegradeDataSource(SentinelProperties properties, ObjectMapper mapper) {
        var nacos = properties.getNacos();
        var source = new NacosDataSource<List<DegradeRule>>(nacos.getServerAddr(), nacos.getGroup(),
                nacos.getDegradeDataId(), converter(mapper, new TypeReference<>() {}));
        DegradeRuleManager.register2Property(source.getProperty());
        return source;
    }

    private <T> Converter<String, T> converter(ObjectMapper mapper, TypeReference<T> type) {
        return value -> {
            try {
                return mapper.readValue(value, type);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Sentinel rule JSON", ex);
            }
        };
    }
}
