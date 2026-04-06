package com.lab.common.constant;

/**
 * 公共常量
 */
public interface CommonConstants {

    // ==================== HTTP Header ====================
    /** TraceId 请求头 */
    String HEADER_TRACE_ID = "X-Trace-Id";
    /** 用户 ID 请求头（网关解析 Token 后透传） */
    String HEADER_USER_ID = "X-User-Id";
    /** 用户名请求头 */
    String HEADER_USER_NAME = "X-User-Name";
    /** 灰度标识请求头 */
    String HEADER_GRAY = "X-Gray";
    /** API 版本请求头 */
    String HEADER_API_VERSION = "X-Api-Version";

    // ==================== 分页默认值 ====================
    int DEFAULT_PAGE_NUM = 1;
    int DEFAULT_PAGE_SIZE = 10;
    int MAX_PAGE_SIZE = 100;

    // ==================== 通用状态 ====================
    /** 正常 */
    int STATUS_NORMAL = 1;
    /** 禁用 */
    int STATUS_DISABLE = 0;
    /** 已删除（逻辑删除） */
    int STATUS_DELETED = -1;

    // ==================== 布尔映射 ====================
    int YES = 1;
    int NO = 0;

    // ==================== MDC Key ====================
    String MDC_TRACE_ID = "traceId";
    String MDC_USER_ID = "userId";
    String MDC_USER_NAME = "userName";
}
