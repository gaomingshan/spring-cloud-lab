package com.lab.observability.tracing.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SkyWalking Agent 验证端点
 * <p>
 * Agent 自动拦截： - HTTP 入口（所有 @RequestMapping 方法） - RestTemplate 出站调用 - 方法级数据库操作（MyBatis / JDBC）
 * <p>
 * 在 SkyWalking UI 中可看到完整的调用链拓扑和每个 Span 的耗时。
 */

/**
 * SkyWalking Agent 验证端点
 * <p>
 * Agent 自动拦截：HTTP 入口、RestTemplate 出站调用、数据库操作。
 * <p>
 * 注意：RestTemplate 必须用 {@code new RestTemplate()} 而非 Spring 注入。 SkyWalking 10.1.0 的 spring-resttemplate-6.x-plugin 对
 * RestTemplateBuilder.build() 存在兼容性问题，拦截 new RestTemplate() 构造器则正常工作。
 */
@Slf4j
@RestController
@RequestMapping("/tracing")
public class AgentVerifyController {


    /**
     * 模拟下单
     */
    @PostMapping("/order")
    public Result<String> createOrder(@RequestParam(defaultValue = "1001") String userId) {
        log.info("创建订单, userId={}", userId);
        return Result.ok("订单创建成功");
    }
}
