package com.lab.observability.tracing.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry SDK 手动配置
 *
 * 配置链路：
 *   OtlpGrpcSpanExporter → BatchSpanProcessor → SdkTracerProvider → OpenTelemetrySdk
 *
 * OTLP Exporter 将 Trace 数据通过 gRPC 发送到 SkyWalking OAP：
 *   SkyWalking OAP 9.x 内置 OTLP gRPC Receiver（端口 11800）
 *   接收后在 SkyWalking UI 中展示链路拓扑图和 Span 详情
 *
 * 生产环境建议：
 *   使用 OTel Java Agent（-javaagent 方式）自动采集，无需手动配置
 *   本配置类用于演示 SDK 手动埋点的原理
 */
@Configuration
public class OtelConfig {

    @Value("${otel.exporter.otlp.endpoint:http://127.0.0.1:11800}")
    private String otlpEndpoint;

    @Value("${spring.application.name:observability-tracing}")
    private String serviceName;

    @Bean
    public OpenTelemetry openTelemetry() {
        // OTLP gRPC Exporter：将 Span 数据发送到 SkyWalking OAP
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();

        // Resource：标识服务名称等元数据
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, serviceName
                )));

        // TracerProvider：管理 Tracer 实例和 Span 处理管道
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();

        // 构建 OpenTelemetry 实例并注册为全局
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        // JVM 关闭时优雅关闭 SDK（刷出缓冲区中的 Span）
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        return openTelemetry;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }
}
