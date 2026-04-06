# lab-multi-datasource - 异构数据源

## 一、核心组件

| 组件 | 说明 |
|------|------|
| MyBatis-Flex | 操作 MySQL 关系型数据 |
| Spring Data MongoDB | 操作 MongoDB 文档型数据 |
| `MongoRepository` | MongoDB CRUD 基础接口 |
| `@Document` | 映射 MongoDB Collection |
| `MongoTemplate` | 复杂 MongoDB 操作（聚合/Pipeline）|

---

## 二、核心能力

### 场景设计
```
用户注册请求
  │
  ├── MySQL（MyBatis-Flex）
  │     用户基础表 t_user（id/name/email/password）
  │     特点：强一致性、支持事务、关联查询
  │
  └── MongoDB（Spring Data MongoDB）
        用户行为日志 user_behavior_log
        特点：高写入、Schema 灵活（extra 字段无限扩展）、无需事务
```

### 两个数据源共存配置原理
- `spring.datasource.*` → HikariCP 连接池 → MyBatis-Flex 自动装配
- `spring.data.mongodb.*` → MongoClient → Spring Data MongoDB 自动装配
- 两套配置完全独立，Bean 名称不冲突

### 事务边界重要说明
- MySQL 事务：`@Transactional` 正常使用，只控制 MySQL 操作
- MongoDB 事务：Spring Data MongoDB 支持 `@Transactional`，但需 MongoDB 副本集
- **跨数据源事务**：无法通过单个 `@Transactional` 保证，需用 Seata AT/TCC 或补偿机制

---

## 三、最佳实践

1. **按数据特性选型**：强一致/关联查询 → MySQL；高写入/灵活Schema → MongoDB
2. **异构双写最终一致**：MySQL 写成功后写 MongoDB，MongoDB 失败走补偿任务重试
3. **不要跨库 JOIN**：异构数据源无法 JOIN，需在应用层聚合
4. **MongoDB 索引提前规划**：`@Indexed` 注解或手动在 MongoDB 控制台创建

---

## 四、演示步骤

```bash
# 1. 启动 MySQL + MongoDB
docker-compose up mysql mongodb

# 2. 初始化 MySQL 库
mysql -u root -proot123 -e "CREATE DATABASE multi_ds;"

# 3. 启动服务
mvn spring-boot:run -pl lab-multi-datasource

# 4. 写入行为日志（MongoDB）
curl -X POST 'http://localhost:8600/multi/log?userId=1&action=LOGIN' \
  -H 'Content-Type: application/json' \
  -d '{"device":"iPhone","ip":"192.168.1.1"}'

# 5. 查询行为日志
curl http://localhost:8600/multi/log/1

# 6. 异构双写演示
curl -X POST 'http://localhost:8600/multi/user-register?userId=1&username=Tom'
```
