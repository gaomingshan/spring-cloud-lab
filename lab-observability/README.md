# lab-observability - 可观测性

> 聚合模块（`packaging=pom`），包含三个独立可运行的子模块，每个子模块聚焦可观测性的一个支柱。

## 一、三大支柱总览

| 支柱 | 子模块 | 技术栈 | 端口 | 职责 |
|------|--------|--------|------|------|
| Metrics（指标） | `observability-metrics` | Micrometer → Prometheus → Grafana | 8701 | 系统/业务指标监控大盘 |
| Tracing（链路追踪） | `observability-tracing` | OpenTelemetry → OTLP → SkyWalking OAP → UI | 8702 | 跨服务调用链路可视化 |
| Logging（日志） | `observability-logging` | Logback JSON → Logstash → Elasticsearch → Kibana | 8703 | 结构化日志聚合检索 |

三者通过 **TraceId** 串联：Kibana 按 TraceId 检索日志 → SkyWalking 查看对应链路图 → Grafana 观察对应时间段指标。

---

## 二、架构全景

```
                        OpenTelemetry（统一遥测标准）
                               │
               ┌───────────────┼───────────────┐
               ▼               ▼               ▼
         Traces(OTLP)    Metrics(Pull)    Logs(JSON)
               │               │               │
               ▼               ▼               ▼
        SkyWalking OAP    Prometheus       Logstash
        (OTLP Receiver)   (Scrape)        (Pipeline)
               │               │               │
               ▼               ▼               ▼
        SkyWalking UI      Grafana        Elasticsearch
                                               │
                                               ▼
                                            Kibana
```

---

## 三、模块目录结构

```
lab-observability/                          ← 聚合 POM（无业务代码）
├── pom.xml
├── README.md                               ← 本文件（总览）
│
├── observability-metrics/                  ← 【Metrics】Micrometer + Prometheus + Grafana
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       └── main/java/com/lab/observability/metrics/
│           ├── MetricsApplication.java
│           └── controller/MetricsController.java
│
├── observability-tracing/                  ← 【Tracing】OpenTelemetry + SkyWalking
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       └── main/java/com/lab/observability/tracing/
│           ├── TracingApplication.java
│           ├── config/OtelConfig.java
│           └── controller/TracingController.java
│
└── observability-logging/                  ← 【Logging】Logback + ELK
    ├── pom.xml
    ├── README.md
    └── src/
        └── main/java/com/lab/observability/logging/
            ├── LoggingApplication.java
            └── controller/LoggingController.java
```

---

## 四、TraceId 串联机制

| 支柱 | TraceId 来源 | 说明 |
|------|-------------|------|
| Tracing | OTel Agent/SDK 生成 | 链路追踪的核心标识 |
| Logging | MDC 注入 `traceId` | 日志中打印 TraceId，Kibana 可按此字段检索 |
| Metrics | Exemplar 关联 | 指标样本可关联到具体 TraceId（Grafana Exemplar 功能） |

---

## 五、快速开始

```bash
# 1. 启动全部基础设施
docker-compose up -d prometheus grafana elasticsearch logstash kibana skywalking-oap skywalking-ui

# 2. 分别启动三个子模块（各独立运行）
cd lab-observability/observability-metrics  && mvn spring-boot:run
cd lab-observability/observability-tracing  && mvn spring-boot:run
cd lab-observability/observability-logging  && mvn spring-boot:run

# 3. 访问各子模块接口
# Metrics
curl -X POST http://localhost:8701/metrics/order
curl http://localhost:8701/actuator/prometheus

# Tracing
curl -X POST "http://localhost:8702/tracing/order?userId=1001"

# Logging
curl "http://localhost:8703/logging/demo?userId=1001"

# 4. 查看监控大盘
# Grafana:    http://localhost:3000  (admin/admin)
# SkyWalking: http://localhost:8080
# Kibana:     http://localhost:5601
# Prometheus: http://localhost:9090
```

---

## 六、外部基础设施

| 基础设施 | 用途 | 端口 |
|---------|------|------|
| Prometheus | 指标采集与存储 | 9090 |
| Grafana | 指标可视化大盘 | 3000 |
| SkyWalking OAP | 链路数据接收（OTLP gRPC） | 11800 / 12800 |
| SkyWalking UI | 链路可视化 | 8080 |
| Elasticsearch | 日志/链路数据存储 | 9200 |
| Logstash | 日志管道（解析/转换） | 5044 / 5000 |
| Kibana | 日志可视化检索 | 5601 |
