package com.lab.observability.tracing.controller;

import com.lab.common.result.Result;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 链路追踪演示 Controller
 *
 * 使用 Micrometer Tracing API（Spring Boot 3 官方推荐的追踪门面）：
 *   - 应用代码面向 io.micrometer.tracing.Tracer 编程（门面层）
 *   - 底层通过 micrometer-tracing-bridge-otel 桥接到 OpenTelemetry SDK
 *   - OpenTelemetry SDK 通过 OTLP Exporter 将数据发送到 SkyWalking OAP
 *
 * 演示内容：
 *   - 父子 Span 嵌套（createOrder → checkInventory → saveOrder）
 *   - Span Tag（业务标签）
 *   - Span Event（关键事件记录）
 *   - Error 状态设置
 *   - TraceId 自动注入 MDC（日志中可用 %X{traceId}）
 *
 * 在 SkyWalking UI 中可以看到：
 *   - 服务拓扑图、Trace 详情、每个 Span 的耗时和标签
 *   - TraceId 可在 Kibana 中检索关联日志
 */
@Slf4j
@RestController
@RequestMapping("/tracing")
@RequiredArgsConstructor
public class TracingController {

    private final Tracer tracer;

    /**
     * 模拟下单 — 演示父子 Span 嵌套
     * 在 SkyWalking UI 中可以看到：createOrder → checkInventory → saveOrder 的调用链
     */
    @PostMapping("/order")
    public Result<String> createOrder(@RequestParam(defaultValue = "1001") String userId) {
        // 创建父 Span
        Span parentSpan = tracer.nextSpan().name("createOrder").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(parentSpan)) {
            parentSpan.tag("user.id", userId);
            parentSpan.event("开始处理订单");

            String traceId = parentSpan.context().traceId();
            log.info("[Tracing] 创建订单, userId={}, traceId={}", userId, traceId);

            // 模拟子调用
            checkInventory();
            saveOrder(userId);

            parentSpan.event("订单处理完成");
            return Result.ok("订单创建成功, traceId=" + traceId);
        } finally {
            parentSpan.end();
        }
    }

    /**
     * 模拟库存检查 — 子 Span
     */
    private void checkInventory() {
        Span span = tracer.nextSpan().name("checkInventory").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("inventory.sku", "SKU-001");
            // 模拟耗时
            Thread.sleep(30 + (long) (Math.random() * 50));
            span.event("库存检查通过");
        } catch (InterruptedException e) {
            span.error(e);
            Thread.currentThread().interrupt();
        } finally {
            span.end();
        }
    }

    /**
     * 模拟保存订单 — 子 Span
     */
    private void saveOrder(String userId) {
        Span span = tracer.nextSpan().name("saveOrder").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("user.id", userId);
            span.tag("db.system", "mysql");
            span.tag("db.operation", "INSERT");
            // 模拟数据库写入耗时
            Thread.sleep(20 + (long) (Math.random() * 40));
            span.event("订单已持久化");
        } catch (InterruptedException e) {
            span.error(e);
            Thread.currentThread().interrupt();
        } finally {
            span.end();
        }
    }

    /**
     * 模拟异常场景 — 演示 Span 错误状态
     * 在 SkyWalking UI 中会标红显示错误链路
     */
    @GetMapping("/error")
    public Result<String> simulateError() {
        Span span = tracer.nextSpan().name("simulateError").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.event("模拟业务异常");
            throw new RuntimeException("模拟的业务异常，用于演示 SkyWalking 错误链路展示");
        } catch (RuntimeException e) {
            span.error(e);
            return Result.fail("模拟异常, traceId=" + span.context().traceId());
        } finally {
            span.end();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        log.info("[Tracing] health check");
        return Result.ok("UP");
    }
}
