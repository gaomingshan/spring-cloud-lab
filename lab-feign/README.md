# lab-feign - 服务调用 + 负载均衡

## 一、核心组件

| 组件 | 说明 |
|------|------|
| OpenFeign | 声明式 HTTP 客户端，基于接口注解生成代理 |
| OkHttp | 替换 Feign 默认 HttpURLConnection，支持连接池/HTTP2 |
| Spring Cloud LoadBalancer | 客户端负载均衡，替代 Ribbon |
| `@FeignClient` | 声明 Feign 客户端，绑定目标服务 |
| `RequestInterceptor` | 全局请求拦截器，注入公共请求头 |
| `fallback` | 降级实现，服务不可用时返回兜底数据 |

---

## 二、核心能力

### 1. 声明式调用
```java
@FeignClient(name = "feign-provider", path = "/provider")
public interface ProviderClient {
    @GetMapping("/hello")
    Result<Map<String, Object>> hello();
}
```
注入 `ProviderClient` 直接调用，无需关心 HTTP 细节。

### 2. 负载均衡策略
Spring Cloud LoadBalancer 默认**轮询**策略，可自定义：
```java
@Bean
public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment env,
        LoadBalancerClientFactory factory) {
    String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    return new RandomLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
}
```

### 3. OkHttp 替换原生客户端
原因：HttpURLConnection 不支持连接池，高并发下性能差；OkHttp 支持连接复用、超时精细控制。
```yaml
feign:
  okhttp:
    enabled: true
```

### 4. 超时配置
```yaml
feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 10000
      feign-provider:    # 针对特定服务覆盖
        read-timeout: 3000
```

### 5. 全局请求头透传（RequestInterceptor）
`FeignRequestInterceptor` 在每次调用前自动注入 TraceId，实现跨服务链路追踪。

### 6. 降级（Fallback）
```java
@FeignClient(name = "feign-provider", fallback = ProviderFallback.class)
```
需配合 Sentinel 或 Resilience4j 使用，`feign.circuitbreaker.enabled=true`。

---

## 三、调用流程

```
Controller
  │
  ▼
ProviderClient（Feign 代理）
  │ FeignRequestInterceptor 注入 TraceId
  ▼
LoadBalancer 选择实例（轮询 8100/8101/8102）
  │
  ▼
OkHttp 发送 HTTP 请求
  │
  ▼
feign-provider（被选中的实例）
```

---

## 四、最佳实践

1. **Feign 接口独立模块**：生产中将 `@FeignClient` 接口抽到单独的 `*-api` 模块，provider 和 consumer 共同依赖
2. **日志级别**：生产用 `BASIC`，开发调试用 `FULL`（含请求体响应体）
3. **超时不要太长**：Feign 超时应小于上游（网关/调用方）超时，避免级联超时
4. **Fallback 不要吞异常**：记录日志，必要时上报监控告警

---

## 五、演示步骤

```bash
# 启动 provider 3个实例
mvn spring-boot:run -pl lab-feign/feign-provider -Dserver.port=8100
mvn spring-boot:run -pl lab-feign/feign-provider -Dserver.port=8101
mvn spring-boot:run -pl lab-feign/feign-provider -Dserver.port=8102

# 启动 consumer
mvn spring-boot:run -pl lab-feign/feign-consumer

# 多次调用，观察 port 轮询变化
for i in {1..6}; do curl http://localhost:8103/consumer/hello; echo; done
```
