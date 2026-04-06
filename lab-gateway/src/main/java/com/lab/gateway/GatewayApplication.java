package com.lab.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动类
 *
 * <p>注意：Gateway 基于 WebFlux（Reactive），不能引入 spring-boot-starter-web
 * 所有 Filter/Handler 必须使用响应式编程风格（Mono/Flux）
 *
 * <p>演示能力：
 * 1. 动态路由（基于 Nacos 注册中心自动路由）
 * 2. 断言工厂（Path/Header/Weight/Method 等多种断言）
 * 3. 过滤器链（GlobalFilter 全局鉴权/TraceId注入）
 * 4. Redis 令牌桶限流（RequestRateLimiter）
 * 5. 灰度路由（Header 染色）
 * 6. Sa-Token 统一鉴权（Reactor 版）
 * 7. API 版本控制（/v1/ /v2/ 路径路由）
 * 8. 签名校验 + 防重放攻击
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
