-- 分库分表演示数据库初始化
CREATE DATABASE IF NOT EXISTS sharding_db0 DEFAULT CHARSET utf8mb4;
CREATE DATABASE IF NOT EXISTS sharding_db1 DEFAULT CHARSET utf8mb4;

-- 每个库创建 2 张分表
USE sharding_db0;
CREATE TABLE IF NOT EXISTS t_order_0 (
    order_id  BIGINT PRIMARY KEY COMMENT '订单ID（Snowflake生成）',
    user_id   BIGINT NOT NULL COMMENT '用户ID（分库键）',
    amount    DECIMAL(10,2) COMMENT '金额',
    status    TINYINT DEFAULT 0 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS t_order_1 LIKE t_order_0;

USE sharding_db1;
CREATE TABLE IF NOT EXISTS t_order_0 LIKE sharding_db0.t_order_0;
CREATE TABLE IF NOT EXISTS t_order_1 LIKE sharding_db0.t_order_0;
