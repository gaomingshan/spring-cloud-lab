# lab-gateway - API 网关

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Spring Cloud Gateway | 基于 WebFlux 的响应式网关 |
| `RouteLocator` / application.yml 路由配置 | 静态路由定义 |
| `GlobalFilter` | 全局过滤器（鉴权、TraceId、限流等） |
| `GatewayFilter` | 路由级过滤器（StripPrefix、RewritePath 等） |
| `RequestRateLimiter` | Redis 令牌桶限流过滤器 |
| Sa-Token Reactor 版 | WebFlux 环境下的认证鉴权 |
| `KeyResolver` | 限流 key 提取策略（按 IP / 用户） |

---

## 二、核心能力

### 1. 动态路由
- **静态路由**：application.yml 中配置，重启生效
- **自动路由**：`discovery.locator.enabled=true`，从 Nacos 自动为每个注册服务创建路由
- **动态路由**（进阶）：将路由规则存入 Nacos，通过 `RouteDefinitionRepository` 实现热更新

### 2. 断言工厂（Predicate）
| 断言类型 | 示例 | 说明 |
|---------|------|------|
| Path | `Path=/api/**` | 路径匹配 |
| Header | `Header=X-Gray, true` | 请求头匹配 |
| Method | `Method=GET,POST` | HTTP 方法 |
| Weight | `Weight=group1, 80` | 权重路由（灰度） |
| Query | `Query=version, v2` | 查询参数 |

### 3. 过滤器链
**全局过滤器（本项目实现）：**
- `TraceIdGlobalFilter`：Order 最高，注入/透传 TraceId
- `AuthGlobalFilter`：Sa-Token 统一鉴权，白名单放行

**内置过滤器（application.yml 配置）：**
- `StripPrefix`：去除路径前缀
- `AddRequestHeader`：添加请求头
- `RequestRateLimiter`：Redis 令牌桶限流
- `DedupeResponseHeader`：去重响应头（解决 CORS 重复头）

### 4. 限流（Redis 令牌桶）
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10   # 每秒补充令牌
      redis-rate-limiter.burstCapacity: 20   # 桶最大容量
      key-resolver: "#{@ipKeyResolver}"      # 按 IP 限流
```

### 5. 灰度路由
通过 Header `X-Gray: true` 将流量路由到灰度实例（结合 Nacos 实例元数据 `version=v2`）。

### 6. API 版本控制
```
GET /v1/users  → registry-provider（v1 服务）
GET /v2/users  → registry-provider-v2（v2 服务）
```

### 7. 签名校验 & 防重放
- 请求携带：`timestamp`、`nonce`（随机数）、`sign`（HMAC-SHA256 签名）
- 网关校验：时间戳有效期（±5分钟）、nonce 已用标记（Redis SET NX，5分钟 TTL）

---

## 三、网关处理流程

```
客户端请求
    │
    ▼
[TraceIdGlobalFilter] → 注入/透传 TraceId（Order: MAX)
    │
    ▼
[AuthGlobalFilter]   → Sa-Token 鉴权，白名单放行（Order: -100）
    │
    ▼
[路由匹配] → Path/Header/Weight 断言匹配路由规则
    │
    ▼
[GatewayFilter链] → StripPrefix、限流、添加Header 等
    │
    ▼
[lb://service-name] → LoadBalancer 选择实例
    │
    ▼
下游微服务
```

---

## 四、最佳实践

1. **不引入 spring-boot-starter-web**：Gateway 基于 WebFlux，两者不兼容
2. **Sa-Token 必须用 Reactor 版**：`sa-token-reactor-spring-boot3-starter`
3. **限流 key 粒度**：生产环境按用户 ID 限流，比按 IP 更精准
4. **路由顺序**：多条路由按 Order 匹配，精确路由应排在通配路由之前
5. **超时配置**：网关默认超时 1s，按业务调整 `connect-timeout` 和 `response-timeout`

---

## 五、配置步骤

### 5.1 启动依赖
```bash
docker-compose up nacos redis
```

### 5.2 启动网关
```bash
mvn spring-boot:run -pl lab-gateway
```

### 5.3 验证路由
```bash
# 通过网关访问 provider（网关端口 8080）
curl http://localhost:8080/api/provider/provider/hello

# 查看所有路由
curl http://localhost:8080/actuator/gateway/routes

# 测试限流（连续请求超过 20 次触发限流）
for i in {1..25}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/provider/provider/hello; done
```
