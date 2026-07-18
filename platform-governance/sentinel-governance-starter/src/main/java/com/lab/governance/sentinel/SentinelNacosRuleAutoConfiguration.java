package com.lab.governance.sentinel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@ConditionalOnClass(NacosDataSource.class)
@ConditionalOnProperty(prefix = "lab.governance.sentinel.nacos", name = "enabled", havingValue = "true")
public class SentinelNacosRuleAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(name = "sentinelFlowDataSource")
    NacosDataSource<List<FlowRule>> sentinelFlowDataSource(SentinelProperties properties, ObjectMapper mapper) {
        var nacos = properties.getNacos();
        var source = new NacosDataSource<List<FlowRule>>(nacosProperties(nacos), nacos.getGroup(),
                nacos.getFlowDataId(), converter(mapper, new TypeReference<>() {}));
        FlowRuleManager.register2Property(source.getProperty());
        return source;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(name = "sentinelDegradeDataSource")
    NacosDataSource<List<DegradeRule>> sentinelDegradeDataSource(SentinelProperties properties, ObjectMapper mapper) {
        var nacos = properties.getNacos();
        var source = new NacosDataSource<List<DegradeRule>>(nacosProperties(nacos), nacos.getGroup(),
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

    private java.util.Properties nacosProperties(SentinelProperties.Nacos nacos) {
        var properties = new java.util.Properties();
        putIfPresent(properties, "serverAddr", nacos.getServerAddr());
        putIfPresent(properties, "namespace", nacos.getNamespace());
        putIfPresent(properties, "username", nacos.getUsername());
        putIfPresent(properties, "password", nacos.getPassword());
        return properties;
    }

    private void putIfPresent(java.util.Properties properties, String key, String value) {
        if (value != null && !value.isBlank()) properties.put(key, value);
    }
}
