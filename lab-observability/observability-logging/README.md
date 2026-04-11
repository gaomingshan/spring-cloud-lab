# observability-logging - 日志聚合

## 技术栈

Logback → JSON 结构化日志 → Logstash → Elasticsearch → Kibana

## 日志流转链路

```
Logback（logstash-logback-encoder 输出 JSON）
  │ MDC 包含 traceId / userId 等上下文字段
  │ 输出到 logs/observability-logging.json.log
  ▼
Filebeat（采集日志文件，轻量级 Agent）
  ▼
Logstash（解析/过滤/转换/富化日志）
  ▼
Elasticsearch（全文索引存储）
  ▼
Kibana（可视化检索，按 traceId / userId / level 筛选）
```

## JSON 日志示例

```json
{
  "@timestamp": "2024-01-01T10:00:00.000Z",
  "level": "INFO",
  "logger_name": "com.lab.observability.logging.controller.LoggingController",
  "message": "[Logging] INFO 级别 - 业务关键节点，userId=1001",
  "app": "observability-logging",
  "env": "dev",
  "traceId": "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4",
  "userId": "1001",
  "thread_name": "http-nio-8703-exec-1"
}
```

异常日志会自动包含 `stack_trace` 字段：

```json
{
  "@timestamp": "2024-01-01T10:00:01.000Z",
  "level": "ERROR",
  "message": "[Logging] 业务处理异常, traceId=...",
  "stack_trace": "java.lang.ArithmeticException: / by zero\n\tat ...",
  "app": "observability-logging",
  "traceId": "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4"
}
```

## 日志级别使用规范

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| **DEBUG** | 开发调试信息，生产环境关闭 | 方法入参、SQL 详情 |
| **INFO** | 业务关键节点，正常流转 | 订单创建成功、用户登录 |
| **WARN** | 潜在问题，不影响主流程 | 重试、降级、慢查询 |
| **ERROR** | 业务异常，需要关注处理 | 支付失败、第三方超时 |

## MDC 上下文字段

| 字段 | 来源 | 说明 |
|------|------|------|
| `traceId` | OTel Agent / SkyWalking Agent 自动注入 | 链路追踪 ID，贯通日志和链路 |
| `tid` | SkyWalking Agent 注入 | SkyWalking 格式的 TraceId |
| `userId` | 业务代码手动注入 `MDC.put("userId", ...)` | 当前操作用户 |

## 演示步骤

```bash
# 1. 启动基础设施
docker-compose up -d elasticsearch logstash kibana

# 2. 启动应用
cd lab-observability/observability-logging
mvn spring-boot:run

# 3. 触发日志生成
# 各级别日志演示
curl "http://localhost:8703/logging/demo?userId=1001"

# 异常日志演示
curl http://localhost:8703/logging/error

# 批量日志（验证 ELK 管道）
curl -X POST "http://localhost:8703/logging/batch?count=50"

# 4. 查看 JSON 日志文件
cat logs/observability-logging.json.log | jq .

# 5. 在 Kibana 中检索
# Kibana：http://localhost:5601
# 创建 Index Pattern：logstash-*
# 按 traceId 字段搜索，关联 SkyWalking 链路图
```

## Logstash 管道配置

Logstash 从 Filebeat 接收日志，解析后写入 Elasticsearch：

```conf
input {
  beats {
    port => 5044
  }
}

filter {
  json {
    source => "message"
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "logstash-%{+YYYY.MM.dd}"
  }
}
```
