package com.lab.governance.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.lab.governance.contract.ServiceDiscovery;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ServiceDiscoveryProperties.class)
@ConditionalOnProperty(prefix = "lab.governance.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceDiscoveryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    ServiceDiscovery serviceDiscovery(NacosDiscoveryProperties nacosProperties, ServiceDiscoveryProperties properties) {
        return new NacosServiceDiscovery(nacosProperties, properties);
    }
}
