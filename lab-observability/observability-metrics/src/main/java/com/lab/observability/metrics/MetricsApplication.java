package com.lab.observability.metrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 指标监控演示启动类
 *
 * 技术栈：Micrometer → Prometheus → Grafana
 *
 * 工作原理：
 * 1. Micrometer 作为「门面」收集应用指标（Counter/Timer/Gauge）
 * 2. micrometer-registry-prometheus 将指标转为 Prometheus 格式
 * 3. Spring Boot Actuator 暴露 /actuator/prometheus 端点
 * 4. Prometheus 定时拉取（Pull 模式，默认 15s）
 * 5. Grafana 连接 Prometheus 数据源，通过 PromQL 查询并可视化
 *
 * 访问地址：
 * - 应用接口：http://localhost:8701/metrics/order
 * - Prometheus 端点：http://localhost:8701/actuator/prometheus
 * - Prometheus UI：http://localhost:9090
 * - Grafana 大盘：http://localhost:3000 (admin/admin)
 */
@EnableDiscoveryClient
@SpringBootApplication
public class MetricsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetricsApplication.class, args);
    }
}
