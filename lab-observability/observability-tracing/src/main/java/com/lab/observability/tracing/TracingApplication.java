package com.lab.observability.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 链路追踪演示启动类
 */
@EnableDiscoveryClient
@SpringBootApplication
public class TracingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracingApplication.class, args);
    }
}
