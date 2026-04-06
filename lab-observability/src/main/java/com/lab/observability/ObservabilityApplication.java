package com.lab.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 可观测性演示启动类
 *
 * 三大支柱：
 * 1. Metrics  - Micrometer 采集 → Prometheus 存储 → Grafana 展示
 *    访问：http://localhost:8700/actuator/prometheus
 *
 * 2. Tracing  - SkyWalking Java Agent（无侵入字节码增强）
 *    启动参数：-javaagent:/path/to/skywalking-agent.jar
 *              -Dskywalking.agent.service_name=lab-observability
 *              -Dskywalking.collector.backend_service=127.0.0.1:11800
 *    无需在代码中添加任何依赖！
 *
 * 3. Logging  - Logback JSON → Filebeat → Logstash → Elasticsearch → Kibana
 *    SkyWalking Agent 自动将 TraceId 写入 MDC key "tid"
 *    logback-spring.xml 中引用 %X{tid} 输出到每条日志
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ObservabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
