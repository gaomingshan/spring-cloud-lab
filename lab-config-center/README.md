# lab-config-center - 配置中心

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Nacos Config | 配置存储与推送中心 |
| `spring-cloud-starter-alibaba-nacos-config` | Spring Cloud 集成 Nacos Config 的 Starter |
| `spring.config.import` | **Spring Boot 3 新方式**，替代已废弃的 `bootstrap.yml` |
| `@RefreshScope` | 标记 Bean 在配置变更时自动销毁重建 |
| `@ConfigurationProperties` | 结构化配置绑定，配合 `@RefreshScope` 使用 |
| `/actuator/refresh` | 手动触发配置刷新端点 |

---

## 二、Spring Boot 3 的重大变化

### 旧版（Spring Boot 2.x）方式 —— 已废弃
```yaml
# bootstrap.yml（需额外引入 spring-cloud-starter-bootstrap 依赖）
spring:
  application:
    name: my-service
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      config:
        file-extension: yaml
```
`bootstrap.yml` 在 Spring Cloud 2020.0+ 默认禁用，需额外引入 `spring-cloud-starter-bootstrap` 才能启用，官方已不推荐。

### 新版（Spring Boot 3 / Spring Cloud 2022+）方式 —— 推荐
```yaml
# application.yml（无需额外依赖，原生支持）
spring:
  application:
    name: my-service
  config:
    import:
      - nacos:my-service.yaml                          # 必须存在
      - optional:nacos:my-service-dev.yaml             # 不存在不报错
      - optional:nacos:common.yaml?group=SHARED_GROUP&refreshEnabled=true
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      config:
        file-extension: yaml
        namespace: ""
        group: DEFAULT_GROUP
```

### 关键差异对比

| 特性 | 旧版（bootstrap.yml）| 新版（spring.config.import）|
|------|---------------------|-----------------------------|
| 额外依赖 | 需要 `spring-cloud-starter-bootstrap` | 不需要 |
| 配置文件 | 独立 bootstrap.yml | 统一在 application.yml |
| 多 DataId | `shared-configs` / `extension-configs` | `spring.config.import` 列表 |
| 可选配置 | 无（不存在则报错）| `optional:` 前缀（不存在不报错）|
| 动态刷新 | `refresh: true` | `refreshEnabled=true` 参数 |
| 优先级控制 | 通过配置顺序 | import 列表从后往前优先级递增 |

---

## 三、核心能力

### 1. 动态配置刷新
Nacos 配置变更后，无需重启服务，自动将新值推送到所有订阅该 DataId 的实例。
- **自动推送**：Nacos Server 监听配置变更，主动推送到客户端（长轮询机制）
- **手动刷新**：`POST /actuator/refresh` 触发 Spring Cloud 上下文刷新
- **刷新条件**：Bean 必须标注 `@RefreshScope` 或使用 `@ConfigurationProperties`

### 2. `spring.config.import` DataId 格式
```
nacos:{dataId}?{参数}

参数说明：
  group=SHARED_GROUP      指定分组（不填用 spring.cloud.nacos.config.group）
  refreshEnabled=true     开启动态刷新（默认 false）
  preference=remote       优先使用远程配置（默认 remote）

示例：
  nacos:my-service.yaml                                  # 必须存在，默认分组
  optional:nacos:my-service-dev.yaml                     # 可选，不存在不报错
  optional:nacos:common.yaml?group=SHARED_GROUP&refreshEnabled=true
```

### 3. 多环境隔离（Namespace）
```yaml
spring:
  cloud:
    nacos:
      config:
        namespace: "dev-namespace-id"   # Nacos 控制台 Namespace 的 ID（非名称）
```
```
Nacos Namespace 隔离：
  ""（空）  → public（默认，本地开发）
  dev-xxx  → 开发环境
  test-xxx → 测试环境
  prod-xxx → 生产环境（配置隔离，互不可见）
```

### 4. 配置优先级（从高到低）
```
spring.config.import 列表中靠后的 DataId
  > spring.config.import 列表中靠前的 DataId
  > 本地 application.yml
```

---

## 四、配置中心架构

```
┌─────────────────────────────────────────────┐
│              Nacos Config Server             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ public   │  │   dev    │  │   prod   │  │
│  │namespace │  │namespace │  │namespace │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────┬─────────────────────────┘
                    │ 长轮询/主动推送
         ┌──────────▼──────────────────────┐
         │       Spring Boot 3 应用         │
         │  application.yml 中声明：        │
         │  spring.config.import:           │
         │    - nacos:my-service.yaml       │
         │    - optional:nacos:common.yaml  │
         └──────────────────────────────────┘
                  配置变更后
         @RefreshScope Bean 自动销毁重建
```

---

## 五、最佳实践

1. **DataId 命名规范**：`{spring.application.name}-{profile}.{ext}`，如 `lab-config-center-dev.yaml`
2. **统一用 `optional:`**：非核心配置加 `optional:` 前缀，避免 DataId 不存在时启动失败
3. **`refreshEnabled=true` 按需开启**：只对需要动态刷新的 DataId 开启，减少不必要的刷新开销
4. **Namespace 用 ID 不用名称**：`namespace` 填写 Nacos 控制台显示的命名空间 ID（UUID 格式），不是名称
5. **敏感配置加密**：生产环境密码等使用 Nacos 加密配置，或结合 Vault/KMS
6. **配置版本管理**：Nacos 控制台支持历史版本回滚，变更前 Nacos 会自动保留历史
7. **本地缓存降级**：Nacos Client 在 `{user.home}/nacos/config` 本地缓存配置，断网时自动降级

---

## 六、演示步骤

```bash
# 1. 启动 Nacos Server
docker-compose up nacos
# 访问 http://localhost:8848/nacos  用户名/密码：nacos/nacos

# 2. 在 Nacos 控制台创建配置
# 配置管理 → 配置列表 → 新建配置
# DataId:   lab-config-center.yaml
# Group:    DEFAULT_GROUP
# 格式:     YAML
# 内容:
app:
  name: from-nacos
  version: 2.0.0
  description: 这是来自 Nacos 的动态配置

# 3. 启动服务
mvn spring-boot:run -pl lab-config-center

# 4. 查看配置是否从 Nacos 加载
curl http://localhost:8081/config/info

# 5. 在 Nacos 控制台修改配置（无需重启）
# 将 version 改为 3.0.0，点击发布

# 6. 触发刷新（或等待自动推送）
curl -X POST http://localhost:8081/actuator/refresh

# 7. 再次查看，version 已变为 3.0.0
curl http://localhost:8081/config/info
```

### 如果仍需使用 bootstrap.yml（兼容旧项目）

需额外引入依赖（**不推荐新项目使用**）：
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```
引入后 bootstrap.yml 恢复生效，但优先级低于 `spring.config.import` 方式。
