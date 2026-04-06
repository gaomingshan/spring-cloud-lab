package com.lab.registry.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class RegistryConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistryConsumerApplication.class, args);
    }

    /**
     * 注册 RestTemplate Bean，添加 @LoadBalanced 使其具备负载均衡能力
     * 之后使用服务名（registry-provider）代替 IP:Port 调用
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
