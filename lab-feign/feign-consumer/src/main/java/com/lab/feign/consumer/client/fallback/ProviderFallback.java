package com.lab.feign.consumer.client.fallback;

import com.lab.common.result.Result;
import com.lab.common.result.ResultCode;
import com.lab.feign.consumer.client.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ProviderClient 降级实现
 * 当 feign-provider 不可用时，返回兜底数据而非抛出异常
 */
@Slf4j
@Component
public class ProviderFallback implements ProviderClient {

    @Override
    public Result<Map<String, Object>> hello() {
        log.warn("[Feign Fallback] feign-provider hello 降级");
        return Result.fail(ResultCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public Result<Map<String, Object>> getUser(Long id) {
        log.warn("[Feign Fallback] feign-provider getUser 降级, id={}", id);
        return Result.fail(ResultCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public Result<String> createOrder(Map<String, Object> body) {
        log.warn("[Feign Fallback] feign-provider createOrder 降级");
        return Result.fail(ResultCode.SERVICE_UNAVAILABLE);
    }
}
