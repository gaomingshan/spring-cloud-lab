package com.lab.observability.tracing.controller;

import com.lab.common.result.Result;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 链路追踪演示 Controller
 *
 * 演示 OpenTelemetry SDK 手动创建 Span：
 *   - 父 Span（接口级别）
 *   - 子 Span（业务方法级别）
 *   - Span Attributes（业务标签）
 *   - Span Events（关键事件记录）
 *   - Error 状态设置
 *
 * Span 通过 OTLP gRPC 发送到 SkyWalking OAP，在 SkyWalking UI 中可以看到：
 *   - 服务拓扑图
 *   - Trace 详情（每个 Span 的耗时、标签、事件）
 *   - TraceId 可在 Kibana 中按此 ID 检索关联日志
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
        Span parentSpan = tracer.spanBuilder("createOrder").startSpan();
        try (Scope scope = parentSpan.makeCurrent()) {
            parentSpan.setAttribute("user.id", userId);
            parentSpan.addEvent("开始处理订单");

            log.info("[Tracing] 创建订单, userId={}, traceId={}", userId, parentSpan.getSpanContext().getTraceId());

            // 模拟子调用
            checkInventory();
            saveOrder(userId);

            parentSpan.addEvent("订单处理完成");
            return Result.ok("订单创建成功, traceId=" + parentSpan.getSpanContext().getTraceId());
        } finally {
            parentSpan.end();
        }
    }

    /**
     * 模拟库存检查 — 子 Span
     */
    private void checkInventory() {
        Span span = tracer.spanBuilder("checkInventory").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("inventory.sku", "SKU-001");
            // 模拟耗时
            Thread.sleep(30 + (long) (Math.random() * 50));
            span.addEvent("库存检查通过");
        } catch (InterruptedException e) {
            span.setStatus(StatusCode.ERROR, "库存检查被中断");
            span.recordException(e);
        } finally {
            span.end();
        }
    }

    /**
     * 模拟保存订单 — 子 Span
     */
    private void saveOrder(String userId) {
        Span span = tracer.spanBuilder("saveOrder").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("user.id", userId);
            span.setAttribute("db.system", "mysql");
            span.setAttribute("db.operation", "INSERT");
            // 模拟数据库写入耗时
            Thread.sleep(20 + (long) (Math.random() * 40));
            span.addEvent("订单已持久化");
        } catch (InterruptedException e) {
            span.setStatus(StatusCode.ERROR, "订单保存被中断");
            span.recordException(e);
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
        Span span = tracer.spanBuilder("simulateError").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.addEvent("模拟业务异常");
            throw new RuntimeException("模拟的业务异常，用于演示 SkyWalking 错误链路展示");
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            return Result.fail("模拟异常, traceId=" + span.getSpanContext().getTraceId());
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
