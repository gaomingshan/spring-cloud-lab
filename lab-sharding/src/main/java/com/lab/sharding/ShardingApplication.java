package com.lab.sharding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分库分表演示启动类
 *
 * 演示能力：
 *   1. 水平分库分表（user_id 分库，order_id 分表）
 *   2. Snowflake 分布式主键自动生成
 *   3. sql-show=true 观察 SQL 路由到哪个物理表
 *   4. 分页查询的特殊处理
 */
@SpringBootApplication
public class ShardingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingApplication.class, args);
    }
}
