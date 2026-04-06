package com.lab.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lab.common.result.Result;
import com.lab.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Sentinel 流控熔断演示 Controller
 *
 * 演示场景：
 *   1. QPS 流控：超过阈值返回 429
 *   2. 熔断降级：慢调用/异常比例触发熔断
 *   3. 热点参数限流：对特定参数值单独限流
 *   4. @SentinelResource：资源定义 + blockHandler + fallback
 */
@Slf4j
@RestController
@RequestMapping("/sentinel")
public class SentinelDemoController {

    /**
     * 演示 QPS 流控
     * Dashboard 配置：资源=hello，阈值类型=QPS，单机阈值=2
     * 连续快速请求超过 2次/秒，触发 blockHandler
     */
    @GetMapping("/hello")
    @SentinelResource(
            value = "hello",
            blockHandler = "helloBlock",
            fallback = "helloFallback"
    )
    public Result<String> hello() {
        return Result.ok("Hello from Sentinel!");
    }

    /** 流控处理（BlockException 子类：FlowException） */
    public Result<String> helloBlock(BlockException e) {
        log.warn("[Sentinel] hello 被流控: {}", e.getClass().getSimpleName());
        return Result.fail(ResultCode.FLOW_LIMIT);
    }

    /** 业务异常降级处理（非 BlockException） */
    public Result<String> helloFallback(Throwable t) {
        log.error("[Sentinel] hello 业务异常降级: {}", t.getMessage());
        return Result.fail(ResultCode.CIRCUIT_BREAKER_OPEN);
    }

    /**
     * 演示熔断降级（慢调用比例）
     * Dashboard 配置：资源=slowApi，熔断策略=慢调用比例，RT=200ms，比例=0.5，熔断时长=10s
     * 让 50% 以上请求超过 200ms，触发熔断
     */
    @GetMapping("/slow")
    @SentinelResource(value = "slowApi", blockHandler = "slowBlock")
    public Result<String> slowApi(@RequestParam(defaultValue = "100") long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
        return Result.ok("slowApi 响应，耗时: " + sleepMs + "ms");
    }

    public Result<String> slowBlock(long sleepMs, BlockException e) {
        log.warn("[Sentinel] slowApi 熔断: sleepMs={}", sleepMs);
        return Result.fail(ResultCode.CIRCUIT_BREAKER_OPEN);
    }

    /**
     * 演示热点参数限流
     * Dashboard 配置：资源=hotspotApi，参数索引=0，单机阈值=5
     * 对 userId=1 设置例外项阈值=1（特殊用户更严格限流）
     */
    @GetMapping("/hotspot")
    @SentinelResource(value = "hotspotApi", blockHandler = "hotspotBlock")
    public Result<String> hotspotApi(@RequestParam String userId) {
        return Result.ok("热点参数演示，userId=" + userId);
    }

    public Result<String> hotspotBlock(String userId, BlockException e) {
        log.warn("[Sentinel] hotspotApi 热点限流: userId={}", userId);
        return Result.fail(ResultCode.FLOW_LIMIT);
    }
}
