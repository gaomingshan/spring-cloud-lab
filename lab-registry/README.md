# lab-registry - 注册中心

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Nacos Server 2.x | 注册中心服务端，存储服务实例信息 |
| `spring-cloud-starter-alibaba-nacos-discovery` | 客户端 Starter，自动注册/发现 |
| `@EnableDiscoveryClient` | 启用服务发现客户端 |
| `DiscoveryClient` | 编程式服务发现 API |
| `@LoadBalanced RestTemplate` | 具备负载均衡能力的 HTTP 客户端 |
| Spring Cloud LoadBalancer | 客户端负载均衡（替代 Ribbon） |

---

## 二、核心能力

### 1. 服务注册
服务启动时，Nacos Client 自动将实例信息（IP、Port、服务名、元数据）注册到 Nacos Server。

### 2. 心跳续约
默认每 5 秒发送一次心跳，超过 15 秒未收到心跳则标记为不健康，超过 30 秒则从注册表删除。

### 3. 服务发现
消费方通过服务名从 Nacos 拉取实例列表（本地缓存 + 监听推送），无需硬编码 IP。

### 4. 健康检查
Nacos 支持客户端心跳（临时实例）和服务端探测（持久实例）两种模式。

### 5. 权重调整
在 Nacos 控制台实时调整实例权重（0~1），LoadBalancer 按权重分发流量，实现平滑发布。

### 6. 元数据
实例可携带自定义 metadata（如 version、zone），用于灰度路由和流量染色。

---

## 三、服务注册与发现流程

```
┌─────────────────┐   1.注册    ┌─────────────────┐
│ provider : 8090 │────────────►│                 │
│ provider : 8091 │────────────►│  Nacos Server   │
└─────────────────┘  2.心跳续约 └────────┬────────┘
                                         │ 3.推送实例列表
                                ┌────────▼────────┐
                                │ registry-       │
                                │ consumer : 8092 │
                                │ （本地缓存实例）  │
                                └─────────────────┘
                                   4. @LoadBalanced
                                   轮询 8090/8091
```

---

## 四、最佳实践

1. **优雅下线**：下线前调用 `DELETE /nacos/v1/ns/instance` 或通过 actuator 关闭，避免流量打到下线实例
2. **元数据灰度**：在 metadata 中增加 `version=v2` 标识灰度实例，路由层按 metadata 过滤
3. **命名空间隔离**：dev/test/prod 使用不同 namespace，防止测试流量污染生产注册表
4. **临时实例 vs 持久实例**：`ephemeral: true`（默认）为临时实例，心跳断开自动剔除；持久实例需手动注销

---

## 五、配置步骤

### 5.1 自动配置（引入 Starter 即生效）
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
`NacosDiscoveryAutoConfiguration` 自动装配，注册 `NamingService`。

### 5.2 最小配置
```yaml
spring:
  application:
    name: my-service       # 服务名（注册到 Nacos 的 serviceName）
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        namespace: ""
        group: DEFAULT_GROUP
        weight: 1.0
        metadata:
          version: v1
```

### 5.3 演示步骤
```bash
# 1. 启动 Nacos Server
docker-compose up nacos

# 2. 启动 provider 实例1
mvn spring-boot:run -pl lab-registry/registry-provider

# 3. 启动 provider 实例2（改端口）
mvn spring-boot:run -pl lab-registry/registry-provider -Dserver.port=8091

# 4. 启动 consumer
mvn spring-boot:run -pl lab-registry/registry-consumer

# 5. 验证负载均衡（多次调用，观察 instancePort 交替变化）
curl http://localhost:8092/consumer/call
curl http://localhost:8092/consumer/instances
```
