package com.lab.observability.controller;

import com.lab.common.result.Result;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 可观测性演示 Controller
 *
 * 演示自定义 Micrometer 指标：
 *   Counter  - 累计计数（订单数、登录次数）
 *   Timer    - 耗时分布（接口响应时间）
 *   Gauge    - 当前值（队列长度、在线人数）
 */
@Slf4j
@RestController
@RequestMapping("/obs")
public class ObservabilityController {

    private final Counter orderCounter;
    private final Timer orderTimer;

    public ObservabilityController(MeterRegistry registry) {
        // 注册自定义 Counter（在 Prometheus 中指标名：lab_order_total）
        this.orderCounter = Counter.builder("lab.order")
                .tag("type", "create")
                .description("订单创建总数")
                .register(registry);
        // 注册自定义 Timer
        this.orderTimer = Timer.builder("lab.order.duration")
                .description("订单处理耗时")
                .register(registry);
    }

    /**
     * 模拟下单接口
     * 每次调用：Counter +1，Timer 记录耗时
     * 在 Grafana 中配置 PromQL: rate(lab_order_total[1m]) 观察下单速率
     */
    @PostMapping("/order")
    public Result<String> createOrder() throws InterruptedException {
        return orderTimer.record(() -> {
            log.info("[Obs] 创建订单，TraceId 已由 SkyWalking Agent 注入 MDC");
            orderCounter.increment();
            try { Thread.sleep(50 + (long)(Math.random() * 100)); } catch (Exception ignore) {}
            return Result.ok("订单创建成功");
        });
    }

    /**
     * 健康检查（SkyWalking 会追踪此接口的调用链路）
     */
    @GetMapping("/health")
    public Result<String> health() {
        log.info("[Obs] health check");
        return Result.ok("UP");
    }
}
