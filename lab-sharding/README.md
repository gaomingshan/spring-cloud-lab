# lab-sharding - 分库分表

## 一、核心组件

| 组件 | 说明 |
|------|------|
| ShardingSphere JDBC 5.x | 客户端分片，无需中间件代理 |
| `shardingsphere-jdbc` | Maven 依赖，自动代理 DataSource |
| INLINE 分片算法 | Groovy 表达式声明分片规则 |
| Snowflake 主键生成 | 分布式唯一 ID，无需数据库自增 |
| `sql-show: true` | 打印逻辑SQL和路由后物理SQL（学习调试） |

---

## 二、核心能力

### 1. 水平分库分表
```
逻辑表：t_order
物理表：ds0.t_order_0 / ds0.t_order_1 / ds1.t_order_0 / ds1.t_order_1

分库键：user_id % 2 → ds0 或 ds1
分表键：order_id % 2 → t_order_0 或 t_order_1
```

### 2. 分片路由类型
| 场景 | 路由结果 |
|------|----------|
| WHERE user_id=1 AND order_id=3 | 精确路由：ds1.t_order_1（1张表）|
| WHERE user_id=1 | 库内全表扫：ds1.t_order_0 + ds1.t_order_1（2张表）|
| WHERE order_id=3 | 全库全表扫：ds0+ds1 各 t_order_1（2张表）|
| 无分片键 | 全路由：4张表全扫 |

### 3. 分布式主键（Snowflake）
ShardingSphere 自动为 `order_id` 列生成 Snowflake ID，业务代码无需手动生成。

### 4. 分片算法类型
| 类型 | 适用 |
|------|------|
| INLINE | 简单取模/哈希，Groovy 表达式 |
| STANDARD | 精确+范围分片（实现接口）|
| COMPLEX | 多列联合分片 |
| HINT | 强制路由（不依赖分片列）|

---

## 三、最佳实践

1. **分片键选择**：选择查询频率最高的列作为分片键，避免全路由
2. **分片数规划**：提前规划好分片数，后期扩容成本极高（需迁移数据）
3. **分页查询**：跨分片分页需归并，建议使用游标分页或限制查询范围
4. **事务**：跨分片事务需使用 ShardingSphere 分布式事务（XA/BASE）
5. **sql-show 生产关闭**：打印 SQL 有性能开销，生产设 `false`

---

## 四、演示步骤

```bash
# 1. 初始化数据库
mysql -u root -proot123 < lab-sharding/sql/init.sql

# 2. 启动服务
mvn spring-boot:run -pl lab-sharding

# 3. 插入订单（观察控制台路由日志）
curl -X POST 'http://localhost:8500/sharding/order?userId=1'
curl -X POST 'http://localhost:8500/sharding/order?userId=2'

# 4. 精确查询（单表路由）
curl 'http://localhost:8500/sharding/order/1001?userId=1'

# 5. 范围查询（库内全表扫）
curl 'http://localhost:8500/sharding/orders?userId=1'
```
