package com.lab.observability.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 链路追踪演示启动类
 *
 * 技术栈：OpenTelemetry SDK → OTLP gRPC → SkyWalking OAP → SkyWalking UI
 *
 * 两种使用方式（本模块都演示）：
 *
 * 方式一：OTel Java Agent（无侵入，推荐生产使用）
 *   java -javaagent:opentelemetry-javaagent.jar \
 *        -Dotel.service.name=observability-tracing \
 *        -Dotel.exporter.otlp.endpoint=http://127.0.0.1:11800 \
 *        -jar observability-tracing.jar
 *   Agent 自动完成 HTTP/JDBC/Redis 等框架的 Span 采集和 TraceId 透传
 *
 * 方式二：OTel SDK 手动埋点（本模块代码演示）
 *   在业务代码中手动创建 Span，适合追踪自定义业务逻辑
 *   通过 OtelConfig 配置 TracerProvider + OTLP Exporter
 *
 * SkyWalking OAP 9.x 原生支持 OTLP gRPC Receiver：
 *   接收 OpenTelemetry 标准格式的 Trace 数据，展示在 SkyWalking UI 中
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
