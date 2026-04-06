package com.lab.common.result;

import lombok.Getter;

/**
 * 统一错误码枚举
 * 规范：2xx 成功，4xx 客户端错误，5xx 服务端错误，6xx 业务错误
 */
@Getter
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 4xx ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ==================== 服务端错误 5xx ====================
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ==================== 业务错误 6xx ====================
    BIZ_ERROR(600, "业务处理失败"),
    DATA_NOT_FOUND(601, "数据不存在"),
    DATA_ALREADY_EXISTS(602, "数据已存在"),
    PARAM_VALIDATE_ERROR(603, "参数校验失败"),
    IDEMPOTENT_ERROR(604, "重复请求，请勿重复提交"),

    // ==================== 分布式事务 ====================
    TRANSACTION_ERROR(700, "分布式事务执行失败"),

    // ==================== 限流熔断 ====================
    FLOW_LIMIT(800, "系统繁忙，请稍后再试"),
    CIRCUIT_BREAKER_OPEN(801, "服务熔断，请稍后再试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
