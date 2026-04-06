package com.lab.registry.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 注册中心演示 - 服务提供方
 *
 * <p>演示步骤：
 * 1. 启动本实例（端口 8090），注册到 Nacos
 * 2. 复制配置，修改端口为 8091，启动第二个实例
 * 3. 在 Nacos 控制台「服务管理 -> 服务列表」观察 registry-provider 有 2 个实例
 * 4. 调整实例权重，观察 consumer 侧负载均衡变化
 */
@EnableDiscoveryClient
@SpringBootApplication
public class RegistryProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistryProviderApplication.class, args);
    }
}
