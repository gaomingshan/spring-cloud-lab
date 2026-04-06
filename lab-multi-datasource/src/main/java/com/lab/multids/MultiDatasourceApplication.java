package com.lab.multids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 异构数据源演示启动类
 *
 * 演示能力：
 *   1. 同一服务同时操作 MySQL（关系型）和 MongoDB（文档型）
 *   2. MySQL（MyBatis-Flex）：用户基础信息，强一致性、支持事务
 *   3. MongoDB（Spring Data MongoDB）：用户行为日志，高写入、灵活 Schema
 *   4. 两个数据源配置互不干扰，各自独立生命周期
 */
@SpringBootApplication
public class MultiDatasourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }
}
