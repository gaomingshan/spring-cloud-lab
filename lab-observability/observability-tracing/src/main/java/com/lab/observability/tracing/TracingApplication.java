package com.lab.observability.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 链路追踪演示启动类
 *
 * <h3>当前方案：SkyWalking Java Agent（零侵入）</h3>
 * <pre>
 * java -javaagent:/path/to/skywalking-agent.jar \
 *      -DSW_AGENT_NAME=observability-tracing \
 *      -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.179.128:11800 \
 *      -jar observability-tracing.jar
 * </pre>
 * Agent 通过字节码增强自动拦截所有 HTTP / RPC / DB 调用，无需在应用代码中
 * 引入任何 Tracing 依赖或手动创建 Span。
 *
 * Agent 获取方式：
 *   Maven 坐标：org.apache.skywalking:skywalking-agent:10.1.0
 *   pom.xml 中已配置 maven-dependency-plugin 自动下载到 target/skywalking/
 *   也可从 https://skywalking.apache.org/downloads/ 手动下载
 *
 * <h3>备用方案：Micrometer Tracing + OTLP/HTTP</h3>
 * 当需要不绑定 SkyWalking、走 Spring Boot 3 标准 OTLP 通道时：
 * <ol>
 *   <li>pom.xml 引入 micrometer-tracing-bridge-otel + opentelemetry-exporter-otlp</li>
 *   <li>代码中使用 io.micrometer.tracing.Tracer API 手动埋点</li>
 *   <li>application.yml 配置 management.otlp.tracing.endpoint → :4318/v1/traces</li>
 *   <li>SkyWalking OAP 启用 SW_RECEIVER_OTEL=default，暴露 4317/4318 端口</li>
 * </ol>
 * 详细配置见 application.yml 注释。
 *
 * <h3>访问地址</h3>
 * <ul>
 *   <li>验证端点：http://localhost:8702/tracing/order</li>
 *   <li>SkyWalking UI：http://localhost:18080</li>
 * </ul>
 */
@EnableDiscoveryClient
@SpringBootApplication
public class TracingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TracingApplication.class, args);
    }
}
