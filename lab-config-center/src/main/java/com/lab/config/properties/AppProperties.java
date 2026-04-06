package com.lab.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * &#064;ConfigurationProperties  方式读取 Nacos 配置
 *
 * <p>与 @Value 对比：
 * - @Value：适合读取单个简单属性
 * - @ConfigurationProperties：适合读取结构化配置对象（推荐）
 *
 * <p>注意：配合 @RefreshScope 才能在配置变更时自动刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 应用名称 */
    private String name;

    /** 应用版本 */
    private String version;

    /** 应用描述 */
    private String description;

    /** 功能开关（演示布尔类型动态配置） */
    private boolean featureEnabled = false;

    /** 限流阈值（演示整数类型动态配置） */
    private int rateLimit = 100;
}
