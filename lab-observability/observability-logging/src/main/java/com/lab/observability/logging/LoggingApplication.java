package com.lab.observability.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 日志聚合演示启动类
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingApplication.class, args);
    }
}
