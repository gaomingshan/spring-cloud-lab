# observability-metrics - 指标监控

## 技术栈

Micrometer → Prometheus → Grafana

## 工作原理

```
业务代码（Micrometer API）
  │ Counter / Timer / Gauge
  ▼
Micrometer Registry（Prometheus 格式转换）
  │
  ▼
/actuator/prometheus 端点
  │ Prometheus 定时拉取（Pull 模式，默认 15s）
  ▼
Prometheus 时序数据库
  │ PromQL 查询
  ▼
Grafana 监控大盘
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
# 1. 启动基础设施
docker-compose up -d prometheus grafana

# 2. 启动应用
cd lab-observability/observability-metrics
mvn spring-boot:run

# 3. 查看 Prometheus 原始指标
curl http://localhost:8701/actuator/prometheus

# 4. 触发业务请求（生成自定义指标数据）
# Counter + Timer
for i in {1..20}; do curl -s -X POST http://localhost:8701/metrics/order; done

# Gauge 入队/出队
curl -X POST http://localhost:8701/metrics/queue/push
curl -X POST http://localhost:8701/metrics/queue/push
curl -X POST http://localhost:8701/metrics/queue/pop

# 查看当前指标统计
curl http://localhost:8701/metrics/stats

# 5. 查看监控大盘
# Prometheus：http://localhost:9090（搜索 lab_order_total）
# Grafana：  http://localhost:3000（admin/admin）
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
