# observability-tracing - 链路追踪

## 技术栈

Micrometer Tracing（门面）→ bridge-otel → OpenTelemetry SDK → OTLP → SkyWalking OAP → UI

## 架构说明

```
业务代码（Micrometer Tracing API）
  │ io.micrometer.tracing.Tracer 创建 Span
  ▼
micrometer-tracing-bridge-otel（桥接层）
  │ 将 Micrometer Span 转换为 OpenTelemetry Span
  ▼
OpenTelemetry SDK（Spring Boot 3 自动配置）
  │ BatchSpanProcessor 批量处理
  ▼
OTLP Exporter（gRPC）
  │ 标准 OTLP 协议发送 Trace 数据
  ▼
SkyWalking OAP（OTLP gRPC Receiver，端口 11800）
  │ 存储到 Elasticsearch
  ▼
SkyWalking UI（链路拓扑图 + Span 详情）
```

## 为什么是 Micrometer Tracing + OpenTelemetry + SkyWalking？

| 维度 | 说明 |
|------|------|
| **Spring Boot 3 官方推荐** | Micrometer Tracing 是 Spring Boot 3 内置的追踪门面，替代了旧的 Spring Cloud Sleuth |
| **门面解耦** | 应用代码只依赖 Micrometer API，底层可切换 OpenTelemetry 或 Brave 实现 |
| **零配置** | Spring Boot 自动配置完成 SDK + Exporter + Processor 的全部接线 |
| **标准化** | 底层使用 OpenTelemetry（CNCF 官方标准），避免厂商锁定 |
| **可视化** | SkyWalking 提供强大的服务拓扑图、Trace 分析、性能指标 |

## 依赖关系

```xml
<!-- ① Actuator：提供 Micrometer 基础设施 -->
spring-boot-starter-actuator

<!-- ② 桥接层：Micrometer Tracing API → OpenTelemetry SDK -->
micrometer-tracing-bridge-otel

<!-- ③ OTLP 导出：将 Trace 数据发送到 SkyWalking OAP -->
opentelemetry-exporter-otlp
```

Spring Boot 3 自动完成：
- 创建 `SdkTracerProvider` + `BatchSpanProcessor`
- 注册 `OtlpGrpcSpanExporter`
- 将 `io.micrometer.tracing.Tracer` 绑定到 OpenTelemetry 底层
- 自动将 TraceId 注入 MDC（日志中可用 `%X{traceId}`）

## 核心配置（application.yml）

```yaml
management:
  tracing:
    sampling:
      probability: 1.0    # 采样率：1.0=100%（生产建议 0.1~0.5）
  otlp:
    tracing:
      endpoint: http://127.0.0.1:11800  # SkyWalking OAP OTLP 地址
```

## Micrometer Tracing API 速查

| 操作 | 代码 |
|------|------|
| 创建 Span | `tracer.nextSpan().name("xxx").start()` |
| 激活 Span | `tracer.withSpan(span)` → SpanInScope（try-with-resources） |
| 添加标签 | `span.tag("key", "value")` |
| 记录事件 | `span.event("事件描述")` |
| 记录异常 | `span.error(exception)` |
| 获取 TraceId | `span.context().traceId()` |
| 结束 Span | `span.end()` |

## 核心概念

| 概念 | 说明 |
|------|------|
| **Trace** | 一次完整的请求链路，由唯一的 TraceId 标识 |
| **Span** | 链路中的一个操作单元，有开始/结束时间 |
| **Parent-Child** | Span 之间的父子关系，形成调用树 |
| **Tag** | Span 上的键值对标签（如 user.id、db.system） |
| **Event** | Span 上的时间点事件（如「库存检查通过」） |

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
