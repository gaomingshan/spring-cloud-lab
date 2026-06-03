package com.lab.observability.metrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 指标监控演示启动类
 */
@EnableDiscoveryClient
@SpringBootApplication
public class MetricsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricsApplication.class, args);
    }
}
