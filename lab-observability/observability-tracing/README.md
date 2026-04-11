# observability-tracing - 链路追踪

## 技术栈

OpenTelemetry SDK → OTLP gRPC → SkyWalking OAP → SkyWalking UI

## 架构说明

```
业务代码（OpenTelemetry API）
  │ 手动创建 Span / Agent 自动采集
  ▼
OpenTelemetry SDK
  │ BatchSpanProcessor 批量处理
  ▼
OTLP gRPC Exporter
  │ 标准 OTLP 协议发送 Trace 数据
  ▼
SkyWalking OAP（OTLP gRPC Receiver，端口 11800）
  │ 存储到 Elasticsearch
  ▼
SkyWalking UI（链路拓扑图 + Span 详情）
```

## 为什么选择 OpenTelemetry + SkyWalking？

| 维度 | 说明 |
|------|------|
| **标准化** | OpenTelemetry 是 CNCF 官方可观测性标准，避免厂商锁定 |
| **可视化** | SkyWalking 提供强大的服务拓扑图、Trace 分析、性能指标 |
| **兼容性** | SkyWalking OAP 9.x 原生支持 OTLP gRPC Receiver |
| **灵活性** | 未来可轻松切换后端（Jaeger、Zipkin 等），只需更换 Exporter |

## 两种使用方式

### 方式一：OTel Java Agent（无侵入，推荐生产使用）

```bash
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.service.name=observability-tracing \
     -Dotel.traces.exporter=otlp \
     -Dotel.exporter.otlp.endpoint=http://127.0.0.1:11800 \
     -jar observability-tracing.jar
```

Agent 自动完成：
- HTTP 请求的 Span 采集和 TraceId 透传
- JDBC、Redis、MQ 等框架的自动追踪
- 将 TraceId 写入 MDC（可与 Logging 模块联动）

### 方式二：OTel SDK 手动埋点（本模块代码演示）

通过 `OtelConfig` 配置 `TracerProvider` + `OTLP Exporter`，在业务代码中手动创建 Span。
适合追踪自定义业务逻辑（如：订单处理流程中的库存检查、订单保存等子步骤）。

## 核心概念

| 概念 | 说明 |
|------|------|
| **Trace** | 一次完整的请求链路，由唯一的 TraceId 标识 |
| **Span** | 链路中的一个操作单元，有开始/结束时间 |
| **Parent-Child** | Span 之间的父子关系，形成调用树 |
| **Attributes** | Span 上的键值对标签（如 user.id、db.system） |
| **Events** | Span 上的时间点事件（如「库存检查通过」） |
| **StatusCode** | Span 状态：OK / ERROR |

## 演示步骤

```bash
# 1. 启动基础设施
docker-compose up -d elasticsearch skywalking-oap skywalking-ui

# 2. 启动应用
cd lab-observability/observability-tracing
mvn spring-boot:run

# 3. 触发正常链路
curl -X POST "http://localhost:8702/tracing/order?userId=1001"

# 4. 触发异常链路（SkyWalking UI 中标红）
curl http://localhost:8702/tracing/error

# 5. 查看链路
# SkyWalking UI：http://localhost:8080
# → Trace 页面搜索 service=observability-tracing
# → 可以看到 createOrder → checkInventory → saveOrder 的调用链
```
