# lab-observability - 可观测性

## 一、三大支柱总览

| 支柱 | 技术栈 | 职责 |
|------|--------|------|
| Metrics（指标） | Micrometer → Prometheus → Grafana | 系统/业务指标监控大盘 |
| Tracing（链路追踪） | SkyWalking Java Agent → OAP → UI | 跨服务调用链路可视化 |
| Logging（日志） | Logback JSON → Filebeat → ELK | 结构化日志聚合检索 |

三者通过 **TraceId** 串联：Kibana 按 TraceId 找日志，SkyWalking 看对应链路图。

---

## 二、Metrics - 指标监控

### 工作原理
```
业务代码（Micrometer API）
  │ Counter/Timer/Gauge
  ▼
Micrometer Registry（Prometheus格式）
  │
  ▼
/actuator/prometheus 端点
  │ Prometheus 定时拉取（pull模式，默认15s）
  ▼
Prometheus 时序数据库
  │ PromQL 查询
  ▼
Grafana 监控大盘
```

### 自动暴露的 Spring Boot 指标
- `http_server_requests`：接口 QPS、耗时、错误率
- `jvm_memory_used_bytes`：JVM 内存
- `hikaricp_connections`：连接池状态
- `system_cpu_usage`：CPU 使用率

### 自定义业务指标（本模块演示）
```java
Counter.builder("lab.order").tag("type", "create").register(registry);
Timer.builder("lab.order.duration").register(registry);
```

---

## 三、Tracing - 链路追踪

### SkyWalking Agent 无侵入方式
**不需要在 pom.xml 添加任何依赖！**

启动参数：
```bash
java -javaagent:/path/to/skywalking-agent/skywalking-agent.jar \
     -Dskywalking.agent.service_name=lab-observability \
     -Dskywalking.collector.backend_service=127.0.0.1:11800 \
     -jar lab-observability.jar
```

Agent 自动完成：
- 跨服务 TraceId 生成与透传（HTTP Header: sw8）
- 将 TraceId 写入 MDC（key: `tid`）
- 数据库、Redis、MQ 调用自动追踪

---

## 四、Logging - 日志体系

### 日志链路
```
Logback（MDC 含 tid=TraceId）
  │ 输出 JSON 格式日志到文件
  ▼
Filebeat（采集日志文件）
  ▼
Logstash（解析/过滤/转换）
  ▼
Elasticsearch（存储）
  ▼
Kibana（按 tid 检索，关联 SkyWalking 链路）
```

### JSON 日志示例
```json
{
  "@timestamp": "2024-01-01T10:00:00.000Z",
  "level": "INFO",
  "logger_name": "com.lab.observability.controller.ObservabilityController",
  "message": "创建订单",
  "app": "lab-observability",
  "tid": "3.145.119.48.15000.1",
  "userId": "1001"
}
```

---

## 五、演示步骤

```bash
# 1. 启动基础设施
docker-compose up prometheus grafana skywalking-oap skywalking-ui elasticsearch kibana

# 2. 启动服务（带 SkyWalking Agent）
java -javaagent:./skywalking-agent/skywalking-agent.jar \
     -Dskywalking.agent.service_name=lab-observability \
     -Dskywalking.collector.backend_service=127.0.0.1:11800 \
     -jar target/lab-observability.jar

# 3. 查看 Prometheus 指标
curl http://localhost:8700/actuator/prometheus

# 4. 触发业务请求（生成指标和链路数据）
for i in {1..10}; do curl -X POST http://localhost:8700/obs/order; done

# 5. 查看监控大盘
# Grafana:    http://localhost:3000  (admin/admin)
# SkyWalking: http://localhost:8080
# Kibana:     http://localhost:5601
```
