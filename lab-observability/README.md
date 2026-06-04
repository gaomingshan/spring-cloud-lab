# lab-observability - 可观测性

> 聚合模块（`packaging=pom`），基于 **LGTM Stack**（Loki + Grafana + Tempo + Mimir）实现可观测性三大支柱的统一监控。

## 一、技术栈总览

| 支柱 | 技术栈 | 端口 | 职责 |
|------|--------|------|------|
| Metrics（指标） | Micrometer → OTel Agent → Alloy → Mimir → Grafana | 8702 | 系统/业务指标监控大盘 |
| Tracing（链路追踪） | OTel Agent → Alloy(OTLP) → Tempo → Grafana | 8702 | 跨服务调用链路可视化 |
| Logging（日志） | Logback → Docker → Alloy(Docker Logs) → Loki → Grafana | 8702 | 结构化日志聚合检索 |

三者通过 **TraceId** 在 Grafana 中无缝串联：Loki 按 TraceId 检索日志 → Tempo 查看对应链路图 → Mimir 观察对应时间段指标。

---

## 二、架构全景

```
                        Spring Boot 应用
                        (Micrometer + OTel Agent)
                               │
               ┌───────────────┼───────────────┐
               ▼               ▼               ▼
         Traces(OTLP)    Metrics(OTLP)    Logs(Docker stdout)
               │               │               │
               ▼               ▼               ▼
                    Alloy（统一采集器）
               │               │               │
               ▼               ▼               ▼
            Tempo            Mimir            Loki
               │               │               │
               └───────────────┼───────────────┘
                               ▼
                           Grafana
                    （统一可视化 + TraceId 串联）
```

---

## 三、模块目录结构

```
lab-observability/                          ← 聚合 POM（无业务代码）
├── pom.xml
├── README.md                               ← 本文件（总览）
│
└── observability-metrics/                  ← 【可观测性】Micrometer + OTel Agent + LGTM Stack
    ├── pom.xml
    ├── README.md
    └── src/
        └── main/java/com/lab/observability/metrics/
            ├── MetricsApplication.java
            └── controller/MetricsController.java
```

---

## 四、TraceId 串联机制

| 支柱 | TraceId 来源 | 说明 |
|------|-------------|------|
| Tracing | OTel Agent 自动生成 | 链路追踪的核心标识 |
| Logging | OTel Agent 注入 MDC `traceId` | 日志中打印 TraceId，Loki 可按此字段检索 |
| Metrics | Exemplar 关联 | 指标样本可关联到具体 TraceId（Grafana Exemplar 功能） |

---

## 五、快速开始

```bash
# 1. 启动 LGTM Stack 基础设施
cd docker/lgtm-stack
docker-compose up -d

# 2. 启动应用（挂载 OTel Agent）
cd lab-observability/observability-metrics
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-javaagent:路径/to/opentelemetry-javaagent.jar \
    -Dotel.service.name=observability-metrics \
    -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
    -Dotel.exporter.otlp.protocol=grpc \
    -Dotel.metrics.exporter=otlp \
    -Dotel.traces.exporter=otlp \
    -Dotel.logs.exporter=none"

# 3. 触发业务请求（生成自定义指标数据）
# Counter + Timer
for i in {1..20}; do curl -s -X POST http://localhost:8702/metrics/order; done

# Gauge 入队/出队
curl -X POST http://localhost:8702/metrics/queue/push
curl -X POST http://localhost:8702/metrics/queue/push
curl -X POST http://localhost:8702/metrics/queue/pop

# 查看当前指标统计
curl http://localhost:8702/metrics/stats

# 4. 查看监控大盘
# Grafana：  http://localhost:3000  (admin/admin)
# Alloy UI： http://localhost:12345
```

---

## 六、外部基础设施（LGTM Stack）

| 基础设施 | 用途 | 端口 |
|---------|------|------|
| Mimir | 指标存储（Prometheus 兼容） | 9009 |
| Loki | 日志聚合存储 | 3100 |
| Tempo | 链路追踪存储 | 3200 |
| Alloy | 统一采集器（OTLP 接收 + Docker 日志） | 12345 / 4317(gRPC) / 4318(HTTP) |
| Grafana | 统一可视化大盘 | 3000 |
