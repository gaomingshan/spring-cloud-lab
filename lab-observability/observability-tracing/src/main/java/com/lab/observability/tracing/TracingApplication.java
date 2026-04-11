package com.lab.observability.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 链路追踪演示启动类
 *
 * 技术栈：Micrometer Tracing（门面）→ bridge-otel → OpenTelemetry SDK → OTLP → SkyWalking OAP → UI
 *
 * Spring Boot 3 官方推荐架构：
 *   1. 应用代码面向 Micrometer Tracing API 编程（io.micrometer.tracing.Tracer）
 *   2. micrometer-tracing-bridge-otel 将 Micrometer API 桥接到 OpenTelemetry SDK
 *   3. opentelemetry-exporter-otlp 通过 OTLP 协议将 Trace 数据发送到 SkyWalking OAP
 *   4. Spring Boot 自动配置完成全部接线，无需手动创建任何 Bean
 *
 * 核心配置（application.yml）：
 *   management.tracing.sampling.probability=1.0  → 采样率
 *   management.otlp.tracing.endpoint=...         → SkyWalking OAP OTLP 地址
 *
 * 访问地址：
 * - 应用接口：http://localhost:8702/tracing/order
 * - SkyWalking UI：http://localhost:8080
 */
@EnableDiscoveryClient
@SpringBootApplication
public class TracingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TracingApplication.class, args);
    }
}
