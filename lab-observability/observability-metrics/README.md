# observability-metrics - 可观测性（LGTM Stack）

## 技术栈

Micrometer + OTel Agent → Alloy → Mimir / Loki / Tempo → Grafana

## 工作原理

```
业务代码（Micrometer API）
  │ Counter / Timer / Gauge
  ▼
Micrometer Registry（Prometheus 格式转换）
  │
  ▼
/actuator/prometheus 端点
  │ OTel Agent 采集（Push 模式，OTLP 协议）
  ▼
Alloy（统一采集器，OTLP Receiver）
  │
  ├── Metrics → Prometheus Remote Write → Mimir（指标存储）
  ├── Traces → OTLP Export → Tempo（链路追踪存储）
  └── Logs  → Docker Logs → Loki（日志聚合存储）
  │
  ▼
Grafana（统一可视化大盘，TraceId 串联三大支柱）
```

## 三种核心指标类型

| 类型 | 说明 | 场景 | Prometheus 指标名 |
|------|------|------|-------------------|
| Counter | 累计计数，只增不减 | 订单总数、登录次数 | `lab_order_total` |
| Timer | 耗时分布，自动计算 P50/P95/P99 | 接口响应时间 | `lab_order_duration_seconds_*` |
| Gauge | 当前值快照，可增可减 | 队列长度、在线人数 | `lab_queue_size` |

## Spring Boot 自动暴露的指标

- `http_server_requests`：接口 QPS、耗时、错误率
- `jvm_memory_used_bytes`：JVM 内存
- `hikaricp_connections`：连接池状态
- `system_cpu_usage`：CPU 使用率

## 演示步骤

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

# 3. 查看 Prometheus 原始指标（仍可通过 actuator 端点查看）
curl http://localhost:8702/actuator/prometheus

# 4. 触发业务请求（生成自定义指标数据）
# Counter + Timer
for i in {1..20}; do curl -s -X POST http://localhost:8702/metrics/order; done

# Gauge 入队/出队
curl -X POST http://localhost:8702/metrics/queue/push
curl -X POST http://localhost:8702/metrics/queue/push
curl -X POST http://localhost:8702/metrics/queue/pop

# 查看当前指标统计
curl http://localhost:8702/metrics/stats

# 5. 查看监控大盘
# Grafana：  http://localhost:3000（admin/admin，数据源已自动配置 Mimir/Loki/Tempo）
# Alloy UI： http://localhost:12345
```

## Grafana PromQL 示例

```promql
# 每分钟下单速率
rate(lab_order_total[1m])

# P95 接口耗时
histogram_quantile(0.95, rate(lab_order_duration_seconds_bucket[5m]))

# 当前队列长度
lab_queue_size

# JVM 内存使用（按区域）
jvm_memory_used_bytes{application="observability-metrics"}
```

## LGTM Stack 组件说明

| 组件 | 用途 | 端口 | 说明 |
|------|------|------|------|
| Mimir | 指标存储 | 9009 | Prometheus 兼容的时序数据库，支持 Remote Write |
| Loki | 日志聚合 | 3100 | 轻量级日志存储，支持 LogQL 查询 |
| Tempo | 链路追踪 | 3200 | 高性能 Trace 存储，支持 OTLP 接收 |
| Alloy | 统一采集器 | 12345 / 4317 / 4318 | Grafana Agent 升级版，统一采集 Metrics/Logs/Traces |
| Grafana | 可视化 | 3000 | 统一大盘，Mimir/Loki/Tempo 数据源已自动 Provisioning |
