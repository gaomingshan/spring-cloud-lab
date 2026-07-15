# lab-seata - 分布式事务

## 一、核心组件

| 组件 | 说明 |
|------|------|
| Seata Server (TC) | 事务协调者，管理全局事务生命周期 |
| `@GlobalTransactional` | 标记全局事务入口（发起方使用）|
| `spring-cloud-starter-alibaba-seata` | Spring Cloud 集成 Seata Starter |
| `undo_log` 表 | AT 模式补偿日志，每个参与方数据库必须创建 |
| Nacos | Seata Server 注册中心 + 配置中心 |

---

## 二、核心能力

### AT 模式（推荐，低侵入）
- 全局事务入口使用 `@GlobalTransactional`，参与方通过 Seata 代理数据源自动记录 undo_log
- 回滚时执行反向 SQL 补偿，对业务完全透明
- 适用：关系型数据库，改造成本低

### TCC 模式（手动，强控制）
- Try（预留资源）→ Confirm（提交）→ Cancel（回滚）
- 需业务实现三个方法，侵入性强
- 适用：跨数据库类型、跨中间件（Redis + MySQL）

---

## 三、AT 模式事务流程

```
seata-order（TM）
  │  @GlobalTransactional → TC 分配 XID
  │  XID 通过 Feign Header 透传
  ├──► seata-storage（RM）记录 undo_log，注册分支
  ├──► seata-account（RM）记录 undo_log，注册分支
  │
  成功 → TC 通知所有 RM 提交（删除 undo_log）
  失败 → TC 通知所有 RM 回滚（undo_log 反向补偿）
```

---

## 四、最佳实践

1. **undo_log 每个参与方库都要建**：见 `sql/init.sql`
2. **tx-service-group 保持一致**：order/account/storage 三个服务配置相同
3. **AT 模式适合 80% 场景**，TCC 适合需要细粒度控制的场景
4. **Seata Server 生产用集群模式** + Nacos 注册

---

## 五、演示步骤

`docker-compose.yml` 只负责通用中间件，不包含 Seata Server。Seata TC 的注册、配置和事务元数据需要与实际 Nacos/MySQL 环境保持一致，应使用你已部署的 TC，或按 Seata 官方 Server 配置独立部署后再执行以下步骤。客户端默认通过 `LAB_NACOS_ADDR`、`LAB_SEATA_GROUP`、`LAB_SEATA_APPLICATION` 发现 TC。

```bash
# 1. 初始化数据库
mysql -u root -proot123 < lab-seata/sql/init.sql

# 2. 启动服务（顺序无要求）
mvn spring-boot:run -pl lab-seata/seata-storage
mvn spring-boot:run -pl lab-seata/seata-account
mvn spring-boot:run -pl lab-seata/seata-order

# 3. 成功场景（三个库同时提交，订单/账户/库存均有真实 SQL 变更）
curl -X POST "http://localhost:8300/order/create?userId=1&productId=1&count=10&money=100"

# 4. 失败回滚场景（storage/account 被回滚，数据恢复原值）
curl -X POST "http://localhost:8300/order/create-fail?userId=1&productId=1&count=10&money=100"

# 5. 验证回滚效果：查看 storage.t_storage、account.t_account 和 order.t_order 数据未变化
```
