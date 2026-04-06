# lab-sentinel - 流控熔断

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Sentinel Core | 流控/熔断核心引擎 |
| `spring-cloud-starter-alibaba-sentinel` | Spring Cloud 集成 Starter，自动拦截 Web 请求 |
| `@SentinelResource` | 声明资源，配置 blockHandler/fallback |
| Sentinel Dashboard | 实时监控、规则动态配置 Web 控制台 |
| `sentinel-datasource-nacos` | 规则持久化到 Nacos（重启不丢失） |

---

## 二、核心能力

### 1. QPS / 线程数流控
- **QPS 流控**：统计每秒请求数，超阈值直接拒绝（快速失败）
- **线程数流控**：统计并发线程数，适合保护慢接口
- **流控效果**：快速失败 / Warm Up（预热） / 匀速排队

### 2. 熔断降级
| 熔断策略 | 说明 |
|---------|------|
| 慢调用比例 | 响应时间 > RT 阈值的比例超过设定值，触发熔断 |
| 异常比例 | 异常请求比例超过设定值，触发熔断 |
| 异常数 | 统计窗口内异常数超过阈值，触发熔断 |

熔断后进入 **OPEN** 状态，经过熔断时长后进入 **HALF_OPEN**，放行一个探测请求，成功则 **CLOSED**，失败则重新 **OPEN**。

### 3. 热点参数限流
对接口的特定参数值进行精细化限流：
- 全局：所有 userId 的请求每秒最多 5 次
- 例外项：userId=VIP 每秒最多 100 次（白名单提升阈值）

### 4. 系统自适应保护
根据 **CPU 使用率 / 系统负载 / 响应时间** 自动触发限流，防止系统雪崩。

### 5. 规则持久化（Nacos）
默认规则存内存，重启丢失。接入 `sentinel-datasource-nacos` 后从 Nacos 加载规则，Dashboard 修改后推送到 Nacos 持久化。

---

## 三、Sentinel 工作原理

```
HTTP 请求
    │
    ▼
[SentinelWebInterceptor] 自动拦截所有 Web 请求
    │
    ▼
[SlotChain 责任链]
  NodeSelectorSlot  → 构建调用树
  ClusterBuilderSlot → 统计集群数据
  StatisticSlot     → 实时统计 QPS/RT/线程数
  FlowSlot          → 流控规则检查 ✓/✗
  DegradeSlot       → 熔断规则检查 ✓/✗
  ParamFlowSlot     → 热点规则检查 ✓/✗
  SystemSlot        → 系统保护检查 ✓/✗
    │
通过 → 执行业务逻辑
拒绝 → BlockException → blockHandler 处理
```

---

## 四、最佳实践

1. **规则持久化必做**：生产必须接 Nacos/ZK 持久化，否则重启规则全丢
2. **blockHandler 和 fallback 分开**：blockHandler 处理限流/熔断，fallback 处理业务异常
3. **资源粒度适中**：不要每行代码都定义资源，以接口/方法为粒度
4. **Dashboard 规则与代码解耦**：规则只在 Dashboard 配置，代码只定义资源名

---

## 五、配置步骤

### 5.1 启动 Sentinel Dashboard
```bash
# 下载：https://github.com/alibaba/Sentinel/releases
java -Dserver.port=8858 -jar sentinel-dashboard-1.8.8.jar
# 访问：http://localhost:8858  账号密码：sentinel/sentinel
```

### 5.2 启动服务
```bash
mvn spring-boot:run -pl lab-sentinel
```

### 5.3 触发限流演示
```bash
# QPS 流控：快速连续请求（超过2次/秒触发）
for i in {1..10}; do curl http://localhost:8200/sentinel/hello; echo; done

# 慢调用熔断：让接口睡眠 300ms
curl "http://localhost:8200/sentinel/slow?sleepMs=300"

# 热点参数限流
curl "http://localhost:8200/sentinel/hotspot?userId=1"
```

### 5.4 Nacos 流控规则 JSON 示例
在 Nacos 创建 DataId=`lab-sentinel-flow-rules`，Group=`SENTINEL_GROUP`：
```json
[
  {
    "resource": "hello",
    "grade": 1,
    "count": 2,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```
