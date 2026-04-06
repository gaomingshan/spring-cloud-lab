package com.lab.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 工具类
 * 负责 TraceId 的生成、存储（MDC + TTL）、透传、清理
 *
 * <p>与 SkyWalking Agent 集成说明：
 * SkyWalking Agent 会自动将 sw8 TraceId 写入 MDC key "tid"，
 * 本工具类在未接入 SkyWalking 时提供降级方案（手动生成 UUID TraceId）
 */
public class TraceIdUtil {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** 使用 TTL 替代 ThreadLocal，保证异步线程也能获取到 TraceId */
    private static final TransmittableThreadLocal<String> TRACE_ID_TTL = new TransmittableThreadLocal<>();

    /**
     * 生成并设置 TraceId
     */
    public static String generateAndSet() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        set(traceId);
        return traceId;
    }

    /**
     * 设置 TraceId（同时写入 TTL 和 MDC）
     */
    public static void set(String traceId) {
        TRACE_ID_TTL.set(traceId);
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取当前 TraceId
     */
    public static String get() {
        return TRACE_ID_TTL.get();
    }

    /**
     * 清理 TraceId（请求结束时调用，防止内存泄漏）
     */
    public static void clear() {
        TRACE_ID_TTL.remove();
        MDC.remove(TRACE_ID_KEY);
    }
}
