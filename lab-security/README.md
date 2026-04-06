# lab-security - 认证鉴权

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Sa-Token | 轻量级 Java 权限认证框架 |
| `StpUtil` | 核心 API：登录/注销/Token/权限 |
| `@SaCheckLogin` | 需要登录才能访问 |
| `@SaCheckPermission` | 需要指定权限码 |
| `@SaCheckRole` | 需要指定角色 |
| `StpInterface` | 自定义权限/角色数据来源 |
| jBCrypt | BCrypt 密码加密 |
| Redis | Token 持久化存储 |

---

## 二、核心能力

### 1. 登录认证
```java
StpUtil.login(userId);           // 登录，生成 Token 存入 Redis
StpUtil.logout();                // 注销，删除 Redis Token
StpUtil.isLogin();               // 判断是否登录
StpUtil.checkLogin();            // 校验登录，未登录抛异常
StpUtil.getLoginId();            // 获取当前登录用户ID
```

### 2. 多端登录（device 隔离）
```java
StpUtil.login(userId, "PC");     // PC端登录
StpUtil.login(userId, "APP");    // APP端登录
StpUtil.logout("PC");            // 只注销PC端
StpUtil.kickout(userId);         // 踢出所有端
```

### 3. 权限认证
```java
// 实现 StpInterface 接口，返回当前用户的权限列表
StpUtil.checkPermission("user:delete");  // 编程式
@SaCheckPermission("user:delete")        // 注解式
```

### 4. 密码加密（jBCrypt）
```java
// 注册时加密存储
String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
// 登录时校验
bool match = BCrypt.checkpw(rawPassword, hashed);
```
BCrypt 每次生成不同 salt，无法反推原文，比 MD5/SHA 安全得多。

### 5. 与 Gateway 集成
- **网关统一鉴权**（推荐）：Gateway 用 `sa-token-reactor` 版做前置鉴权，下游服务信任网关透传的 `X-User-Id`
- **服务内鉴权**：每个服务独立接 Sa-Token，适合服务间直调场景

---

## 三、演示步骤

```bash
# 1. 启动 Redis
docker-compose up redis

# 2. 启动认证服务
mvn spring-boot:run -pl lab-security/auth-server

# 3. 启动资源服务
mvn spring-boot:run -pl lab-security/resource-server

# 4. 登录获取 Token
curl -X POST 'http://localhost:8900/auth/login?username=admin&password=123456'
# 返回：{"tokenName":"Authorization","tokenValue":"xxx-token-xxx",...}

# 5. 携带 Token 访问资源服务（资源服务与认证服务共享 Redis，Token 互通）
curl http://localhost:8901/resource/user-info \
  -H 'Authorization: xxx-token-xxx'

# 6. 测试权限校验
curl -X DELETE http://localhost:8901/resource/user/1 \
  -H 'Authorization: xxx-token-xxx'

# 7. 注销
curl -X POST http://localhost:8900/auth/logout \
  -H 'Authorization: xxx-token-xxx'

# 8. 再次访问（Token 已失效，返回 401）
curl http://localhost:8901/resource/user-info \
  -H 'Authorization: xxx-token-xxx'
```
