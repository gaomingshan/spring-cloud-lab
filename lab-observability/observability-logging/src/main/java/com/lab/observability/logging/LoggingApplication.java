package com.lab.observability.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 日志聚合演示启动类
 *
 * 技术栈：Logback → JSON 结构化日志 → Logstash → Elasticsearch → Kibana
 *
 * 日志流转链路：
 * 1. Logback 使用 logstash-logback-encoder 输出 JSON 格式日志到文件
 * 2. Filebeat 采集日志文件，发送到 Logstash
 * 3. Logstash 解析/过滤/转换日志，写入 Elasticsearch
 * 4. Kibana 提供可视化检索界面，支持按 TraceId 关联链路追踪
 *
 * TraceId 串联：
 *   SkyWalking Agent / OpenTelemetry Agent 自动将 TraceId 写入 MDC
 *   Logback pattern 引用 %X{traceId} / %X{tid}，每条日志携带 TraceId
 *   在 Kibana 中按 TraceId 检索 → 在 SkyWalking UI 中查看对应链路图
 *
 * 访问地址：
 * - 应用接口：http://localhost:8703/logging/demo
 * - Kibana：http://localhost:5601
 * - Elasticsearch：http://localhost:9200
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LoggingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingApplication.class, args);
    }
}
