# lab-stream - 消息队列

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Spring Cloud Stream | 消息中间件抽象层，屏蔽底层 MQ 差异 |
| RocketMQ Binder | 将 Stream 抽象绑定到 RocketMQ 实现 |
| `RocketMQTemplate` | 原生 RocketMQ 客户端（发送延迟/事务消息） |
| 函数式编程模型 | `Consumer<T>` / `Supplier<T>` / `Function<T,R>` |
| 死信队列（DLQ） | 消费失败超过重试次数后转入 DLQ |

---

## 二、核心能力

### 1. 普通消息
发布/订阅模式，producer 发送，consumer 组内竞争消费。

### 2. 延迟消息
RocketMQ 支持 18 个延迟级别（1s→2h），consumer 在指定时间后才收到消息。
常用场景：订单超时取消（30分钟未支付自动关闭）。

### 3. 顺序消息
相同 `hashKey` 的消息路由到同一 Queue，保证该 key 下消息的消费顺序。

### 4. 消费幂等
网络重试可能导致消息重复投递，消费端通过消息 ID + Redis SET NX 实现幂等。

### 5. 死信队列（DLQ）
消费失败重试 N 次后，消息进入 `%DLQ%{consumerGroup}` Topic，需人工处理或定时补偿。

---

## 三、Stream 抽象层架构

```
Producer 代码
  │ StreamBridge.send() / @Bean Supplier<T>
  ▼
Spring Cloud Stream Binder 抽象层
  │ 解耦业务代码与 MQ 实现
  ▼
RocketMQ Binder（或 Kafka Binder，切换只需改依赖+配置）
  │
  ▼
RocketMQ Broker
  │
  ▼
RocketMQ Binder（Consumer 侧）
  ▼
@Bean Consumer<T> 业务消费逻辑
```

---

## 四、最佳实践

1. **消费幂等必做**：消息可能重复，消费逻辑必须幂等（数据库唯一键/Redis NX）
2. **消费组隔离**：不同业务使用不同 group，避免互相影响
3. **DLQ 监控告警**：DLQ 有消息时触发告警，人工介入处理
4. **事务消息用于最终一致**：先发 half 消息，本地事务成功后 commit，失败则 rollback

---

## 五、演示步骤

```bash
# 1. 启动 RocketMQ（docker-compose）
docker-compose up rocketmq-namesrv rocketmq-broker

# 2. 启动 consumer（先启动，避免漏消息）
mvn spring-boot:run -pl lab-stream/stream-consumer

# 3. 启动 producer
mvn spring-boot:run -pl lab-stream/stream-producer

# 4. 发送普通消息
curl -X POST http://localhost:8400/producer/send \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD001","amount":100}'

# 5. 发送延迟消息（10s 后消费方才收到）
curl -X POST 'http://localhost:8400/producer/send-delay?delayLevel=3' \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD002","type":"delay"}'

# 6. 发送顺序消息（相同 orderId 保证顺序）
curl -X POST 'http://localhost:8400/producer/send-ordered?orderId=ORD003' \
  -H 'Content-Type: application/json' \
  -d '{"step":"create"}'
```
