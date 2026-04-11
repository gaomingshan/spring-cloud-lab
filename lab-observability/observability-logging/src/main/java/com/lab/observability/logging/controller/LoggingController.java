package com.lab.observability.logging.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 日志聚合演示 Controller
 *
 * 演示内容：
 *   1. 结构化 JSON 日志输出（logstash-logback-encoder）
 *   2. MDC 上下文传递（traceId、userId 等业务字段）
 *   3. 不同日志级别的使用规范
 *   4. 异常日志的结构化记录
 *
 * JSON 日志输出到文件后：
 *   Filebeat 采集 → Logstash 解析 → Elasticsearch 存储 → Kibana 检索
 *
 * 在 Kibana 中可以按 traceId 字段检索，关联 SkyWalking 链路追踪
 */
@Slf4j
@RestController
@RequestMapping("/logging")
public class LoggingController {

    /**
     * 演示 MDC 上下文注入 + 各级别日志输出
     * 生成的 JSON 日志中会包含 traceId 和 userId 字段
     */
    @GetMapping("/demo")
    public Result<String> demo(@RequestParam(defaultValue = "1001") String userId) {
        // 模拟 TraceId（生产环境由 OTel Agent / SkyWalking Agent 自动注入）
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);
        MDC.put("userId", userId);

        try {
            log.debug("[Logging] DEBUG 级别 - 详细调试信息，生产环境关闭");
            log.info("[Logging] INFO 级别 - 业务关键节点，userId={}", userId);
            log.warn("[Logging] WARN 级别 - 潜在问题，如重试、降级");

            return Result.ok("日志演示完成, traceId=" + traceId);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 演示异常日志的结构化记录
     * logstash-logback-encoder 会自动将异常堆栈序列化为 JSON 的 stack_trace 字段
     */
    @GetMapping("/error")
    public Result<String> errorDemo() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);

        try {
            log.info("[Logging] 开始处理业务逻辑");
            // 模拟异常
            int result = 1 / 0;
            return Result.ok("不会执行到这里");
        } catch (ArithmeticException e) {
            // ERROR 级别：业务异常，需要关注和处理
            // logstash-logback-encoder 自动将异常堆栈序列化到 JSON
            log.error("[Logging] 业务处理异常, traceId={}", traceId, e);
            return Result.fail("异常已记录, traceId=" + traceId);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 演示批量日志生成（用于验证 ELK 数据管道是否通畅）
     */
    @PostMapping("/batch")
    public Result<String> batchLog(@RequestParam(defaultValue = "10") int count) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);

        try {
            for (int i = 1; i <= count; i++) {
                log.info("[Logging] 批量日志 {}/{}, traceId={}", i, count, traceId);
            }
            return Result.ok("已生成 " + count + " 条日志, traceId=" + traceId);
        } finally {
            MDC.clear();
        }
    }
}
