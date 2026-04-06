package com.lab.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 配置中心演示启动类
 *
 * <p>演示能力：
 * 1. Nacos Config 动态配置刷新（@RefreshScope）
 * 2. 多环境配置（dev/test/prod namespace 隔离）
 * 3. 共享配置（shared-configs）
 * 4. 配置优先级：Nacos > 本地 application.yml > bootstrap.yml 默认值
 *
 * <p>启动前提：Nacos Server 已在 127.0.0.1:8848 运行
 */
@SpringBootApplication
public class ConfigCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigCenterApplication.class, args);
    }
}
