package com.lab.observability.metrics.controller;

import com.lab.common.result.Result;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指标监控演示 Controller
 *
 * 演示三种核心 Micrometer 指标类型：
 *   Counter - 累计计数（只增不减，如：订单总数、登录次数）
 *   Timer   - 耗时分布（记录操作时长，自动计算 P50/P95/P99）
 *   Gauge   - 当前值快照（可增可减，如：队列长度、在线人数）
 *
 * 对应 Prometheus 指标名（自动转换 . → _）：
 *   lab_order_total          → Counter
 *   lab_order_duration_*     → Timer（seconds_count / seconds_sum / seconds_max）
 *   lab_queue_size           → Gauge
 *
 * Grafana PromQL 示例：
 *   rate(lab_order_total[1m])                      → 每分钟下单速率
 *   histogram_quantile(0.95, lab_order_duration_*)  → P95 耗时
 *   lab_queue_size                                  → 当前队列长度
 */
@Slf4j
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final Counter orderCounter;
    private final Timer orderTimer;
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public MetricsController(MeterRegistry registry) {
        // 注册 Counter（在 Prometheus 中指标名：lab_order_total）
        this.orderCounter = Counter.builder("lab.order")
                .tag("type", "create")
                .description("订单创建总数")
                .register(registry);

        // 注册 Timer（在 Prometheus 中：lab_order_duration_seconds_*）
        this.orderTimer = Timer.builder("lab.order.duration")
                .description("订单处理耗时")
                .register(registry);

        // 注册 Gauge（在 Prometheus 中：lab_queue_size）
        Gauge.builder("lab.queue.size", queueSize, AtomicInteger::get)
                .description("当前待处理队列长度")
                .register(registry);
    }

    /**
     * 模拟下单 — 演示 Counter + Timer
     * Counter 每次调用 +1，Timer 记录每次耗时
     */
    @PostMapping("/order")
    public Result<String> createOrder() {
        return orderTimer.record(() -> {
            log.info("[Metrics] 创建订单，Counter +1，Timer 记录耗时");
            orderCounter.increment();
            // 模拟业务耗时 50~150ms
            try { Thread.sleep(50 + (long) (Math.random() * 100)); } catch (Exception ignore) {}
            return Result.ok("订单创建成功，当前总数: " + (long) orderCounter.count());
        });
    }

    /**
     * 模拟入队 — 演示 Gauge 增加
     */
    @PostMapping("/queue/push")
    public Result<String> pushQueue() {
        int size = queueSize.incrementAndGet();
        log.info("[Metrics] 入队，当前队列长度: {}", size);
        return Result.ok("入队成功，队列长度: " + size);
    }

    /**
     * 模拟出队 — 演示 Gauge 减少
     */
    @PostMapping("/queue/pop")
    public Result<String> popQueue() {
        int size = queueSize.updateAndGet(v -> Math.max(0, v - 1));
        log.info("[Metrics] 出队，当前队列长度: {}", size);
        return Result.ok("出队成功，队列长度: " + size);
    }

    /**
     * 查看当前所有自定义指标的值
     */
    @GetMapping("/stats")
    public Result<String> stats() {
        String info = String.format("订单总数: %d, 队列长度: %d",
                (long) orderCounter.count(), queueSize.get());
        return Result.ok(info);
    }
}
