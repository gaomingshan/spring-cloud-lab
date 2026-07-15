# lab-commerce - 分布式业务主线

`lab-commerce` 不是另一个孤立的组件示例，而是把已有治理能力串成一个可观察、可注入故障的下单流程。

## 业务链路

```text
Gateway -> commerce-order -> Nacos/LoadBalancer -> seata-storage + seata-account
                               |                         |
                               +---- Seata AT -----------+
                               |
                               +---- local order + Outbox -> RocketMQ
```

订单、Outbox 事件在同一个本地事务中写入；库存和账户通过 Seata AT 参与全局事务。消息只由后台 Outbox 发布器异步投递，避免将 RocketMQ 发送耦合到全局事务。消费者必须以 `requestId` 做幂等键。

## 运行契约

应用在本机启动，中间件运行在 Docker 或局域网均可。`application.yml` 默认连接本机 Docker，局域网环境通过环境变量覆盖：

```powershell
$env:LAB_NACOS_ADDR = "192.168.26.128:8848"
$env:LAB_MYSQL_HOST = "192.168.26.128"
$env:LAB_MYSQL_PASSWORD = "root_password"
$env:LAB_ROCKETMQ_NAMESRV = "192.168.26.128:9876"
```

初始化一次：

```bash
mysql -u root -proot123 < lab-seata/sql/init.sql
```

按顺序启动 `seata-storage`、`seata-account`、`lab-commerce`。Seata Server 必须已经注册到 Nacos，且 `tx-service-group` 映射保持为 `my_tx_group -> default`。

## 验证场景

```bash
# 成功：库存、余额、订单、Outbox 共同提交
curl -X POST http://localhost:8800/commerce/orders -H "Content-Type: application/json" -d '{"requestId":"order-001","userId":1,"productId":1,"count":2,"money":100}'

# 重复请求：按 requestId 返回既有订单，避免重复扣减
curl -X POST http://localhost:8800/commerce/orders -H "Content-Type: application/json" -d '{"requestId":"order-001","userId":1,"productId":1,"count":2,"money":100}'

# 故障注入：库存、余额、订单、Outbox 应全部回滚
curl -X POST http://localhost:8800/commerce/orders/fail -H "Content-Type: application/json" -d '{"requestId":"order-rollback-001","userId":1,"productId":1,"count":2,"money":100}'

# 观察订单与消息发布状态
curl http://localhost:8800/commerce/orders/order-001
```

## 本模块验证的边界

| 能力 | 验证点 |
|---|---|
| Nacos | `commerce-order`、账户、库存的注册和 Feign 发现 |
| OpenFeign/LoadBalancer | 订单服务对参与方的服务名调用 |
| Seata AT | 成功提交、故障全局回滚、各库 `undo_log` |
| 幂等 | `t_commerce_order.request_id` 唯一约束 |
| 最终一致性 | Outbox 失败重试与异步 RocketMQ 发布 |
| OTel + LGTM | 网关到服务调用的 Trace、日志和指标关联 |
