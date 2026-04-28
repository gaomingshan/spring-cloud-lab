package com.lab.observability.tracing.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * SkyWalking Agent 验证端点
 *
 * Agent 自动拦截：
 *   - HTTP 入口（所有 @RequestMapping 方法）
 *   - RestTemplate 出站调用
 *   - 方法级数据库操作（MyBatis / JDBC）
 *
 * 在 SkyWalking UI 中可看到完整的调用链拓扑和每个 Span 的耗时。
 */
@Slf4j
@RestController
@RequestMapping("/tracing")
public class AgentVerifyController {

    private final RestTemplate restTemplate;

    public AgentVerifyController() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 模拟下单 — Agent 会自动为以下操作创建 Span：
     *   1. HTTP 入口：POST /tracing/order
     *   2. 内部方法调用：checkInventory()、saveOrder()
     *   3. 出站 HTTP 调用：RestTemplate GET 请求
     */
    @PostMapping("/order")
    public Result<String> createOrder(@RequestParam(defaultValue = "1001") String userId) {
        log.info("创建订单, userId={}", userId);

        checkInventory(userId);
        saveOrder(userId);

        // 模拟跨服务调用，Agent 自动为 RestTemplate 创建 Exit Span
        try {
            String healthResp = restTemplate.getForObject(
                "http://localhost:8702/tracing/health", String.class);
            log.info("health check response: {}", healthResp);
        } catch (Exception e) {
            log.warn("RestTemplate 调用异常（预期内，演示用）: {}", e.getMessage());
        }

        return Result.ok("订单创建成功");
    }

    private void checkInventory(String userId) {
        log.info("检查库存, userId={}", userId);
        sleep(30 + (long) (Math.random() * 50));
    }

    private void saveOrder(String userId) {
        log.info("保存订单, userId={}", userId);
        sleep(20 + (long) (Math.random() * 40));
    }

    @GetMapping("/error")
    public Result<String> simulateError() {
        log.error("模拟业务异常");
        try {
            throw new RuntimeException("模拟异常，演示 SkyWalking 错误链路展示");
        } catch (RuntimeException e) {
            return Result.fail("模拟异常发生");
        }
    }

    @GetMapping("/health")
    public Result<String> health() {
        log.info("health check");
        return Result.ok("UP");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
