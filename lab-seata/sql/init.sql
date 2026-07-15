-- =========================================================
-- Seata 演示数据库初始化脚本
-- 执行顺序：先执行此脚本，再启动各服务
-- =========================================================

-- 1. 订单库
CREATE DATABASE IF NOT EXISTS seata_order DEFAULT CHARSET utf8mb4;
USE seata_order;

CREATE TABLE IF NOT EXISTS `t_order` (
    `id`         BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT(11) DEFAULT NULL COMMENT '用户ID',
    `product_id` BIGINT(11) DEFAULT NULL COMMENT '产品ID',
    `count`      INT(11)    DEFAULT NULL COMMENT '数量',
    `money`      DECIMAL(11, 0) DEFAULT NULL COMMENT '金额',
    `status`     INT(1)     DEFAULT NULL COMMENT '订单状态：0创建中 1已完结'
) ENGINE = InnoDB AUTO_INCREMENT = 7 DEFAULT CHARSET = utf8;

-- AT 模式必须的 undo_log 表（每个参与方数据库都需要）
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = 'AT transaction mode undo table';

-- 2. 库存库
CREATE DATABASE IF NOT EXISTS seata_storage DEFAULT CHARSET utf8mb4;
USE seata_storage;

CREATE TABLE IF NOT EXISTS `t_storage` (
    `id`         BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT(11) DEFAULT NULL COMMENT '产品ID',
    `total`      INT(11)    DEFAULT NULL COMMENT '总库存',
    `used`       INT(11)    DEFAULT NULL COMMENT '已用库存',
    `residue`    INT(11)    DEFAULT NULL COMMENT '剩余库存'
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8;

INSERT IGNORE INTO t_storage VALUES (1, 1, 100, 0, 100);

CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id`     BIGINT       NOT NULL,
    `xid`           VARCHAR(128) NOT NULL,
    `context`       VARCHAR(128) NOT NULL,
    `rollback_info` LONGBLOB     NOT NULL,
    `log_status`    INT(11)      NOT NULL,
    `log_created`   DATETIME(6)  NOT NULL,
    `log_modified`  DATETIME(6)  NOT NULL,
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3. 账户库
CREATE DATABASE IF NOT EXISTS seata_account DEFAULT CHARSET utf8mb4;
USE seata_account;

CREATE TABLE IF NOT EXISTS `t_account` (
    `id`      BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT(11) DEFAULT NULL COMMENT '用户ID',
    `total`   DECIMAL(10, 0) DEFAULT NULL COMMENT '总额度',
    `used`    DECIMAL(10, 0) DEFAULT NULL COMMENT '已用余额',
    `residue` DECIMAL(10, 0) DEFAULT NULL COMMENT '剩余可用额度'
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8;

INSERT IGNORE INTO t_account VALUES (1, 1, 1000, 0, 1000);

CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL,
    `xid` VARCHAR(128) NOT NULL,
    `context` VARCHAR(128) NOT NULL,
    `rollback_info` LONGBLOB NOT NULL,
    `log_status` INT NOT NULL,
    `log_created` DATETIME(6) NOT NULL,
    `log_modified` DATETIME(6) NOT NULL,
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 4. 业务主线订单库：本地事务中的订单与 Outbox 事件必须原子写入。
CREATE DATABASE IF NOT EXISTS commerce_order DEFAULT CHARSET utf8mb4;
USE commerce_order;

CREATE TABLE IF NOT EXISTS `t_commerce_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `request_id` VARCHAR(64) NOT NULL COMMENT '客户端幂等键',
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `count` INT NOT NULL,
    `money` DECIMAL(10, 2) NOT NULL,
    `status` VARCHAR(32) NOT NULL COMMENT 'CREATED/FAILED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_commerce_order_request_id` (`request_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `t_outbox_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `aggregate_type` VARCHAR(64) NOT NULL,
    `aggregate_id` VARCHAR(64) NOT NULL,
    `event_type` VARCHAR(64) NOT NULL,
    `payload` JSON NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `retry_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `published_at` DATETIME NULL,
    KEY `idx_outbox_status_id` (`status`, `id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
