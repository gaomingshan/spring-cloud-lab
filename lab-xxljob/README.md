# lab-xxljob - 分布式任务调度

## 一、核心组件

| 组件 | 说明 |
|------|------|
| XXL-Job Admin | 调度中心（Web控制台），管理任务配置、触发、监控 |
| XXL-Job Executor | 执行器（本服务），接收并执行任务 |
| `@XxlJob` | 声明任务 Handler，绑定 Admin 中的任务配置 |
| `XxlJobHelper` | 获取分片参数、写执行日志、标记失败 |
| `XxlJobSpringExecutor` | Spring 集成配置，自动注册执行器 |

---

## 二、核心能力

### 1. 任务模式
| 模式 | 说明 |
|------|------|
| Bean 模式 | `@XxlJob` 注解方法，推荐 |
| GLUE 模式 | 在 Admin 控制台直接编写 Java/Shell 代码，热更新 |

### 2. 路由策略
| 策略 | 说明 |
|------|------|
| 轮询 | 轮流选择执行器实例 |
| 随机 | 随机选择 |
| 故障转移 | 选可用实例 |
| 分片广播 | 所有实例同时执行，各处理一部分数据 |
| 一致性HASH | 相同参数固定路由到同一实例 |

### 3. 分片广播（核心能力）
```java
int shardIndex = XxlJobHelper.getShardIndex(); // 当前实例分片号（0/1/2）
int shardTotal = XxlJobHelper.getShardTotal(); // 总分片数
// SQL: WHERE id % shardTotal = shardIndex
```
3个执行器实例并行处理 300万数据，每个只处理 100万，效率提升 3倍。

### 4. 失败重试 & 告警
- Admin 配置失败重试次数（默认0）
- 任务失败/超时发送邮件告警（配置 Admin 邮箱）

---

## 三、演示步骤

```bash
# 1. 启动 XXL-Job Admin
docker-compose up xxljob-admin
# 访问 http://localhost:8858/xxl-job-admin  用户名/密码：admin/123456

# 2. 启动执行器
mvn spring-boot:run -pl lab-xxljob

# 3. Admin 控制台操作
#   a. 执行器管理 → 新增执行器：AppName=lab-xxljob-executor
#   b. 任务管理 → 新增任务：JobHandler=simpleJob，Cron=0/10 * * * * ?
#   c. 操作 → 执行一次（立即触发）
#   d. 查看调度日志，观察任务执行结果

# 4. 演示分片广播
#   a. 启动3个执行器实例（不同端口）
#   b. 新增任务：JobHandler=shardingJob，路由策略=分片广播
#   c. 观察3个实例各自打印的 shardIndex/shardTotal
```
