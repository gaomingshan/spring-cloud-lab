package com.lab.feign.consumer.interceptor;

import com.lab.common.constant.CommonConstants;
import com.lab.common.util.TraceIdUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Feign 全局请求拦截器
 * 职责：在每次 Feign 调用前，自动将 TraceId / Token 等公共请求头注入到下游请求
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 1. 透传 TraceId
        String traceId = TraceIdUtil.get();
        if (StringUtils.hasText(traceId)) {
            template.header(CommonConstants.HEADER_TRACE_ID, traceId);
        }
        // 2. 可在此处透传 Token（从 ThreadLocal 或 RequestContextHolder 获取）
        // String token = TokenContextHolder.get();
        // if (StringUtils.hasText(token)) {
        //     template.header("Authorization", token);
        // }
        log.debug("[Feign Interceptor] 注入请求头 traceId={}", traceId);
    }
}
