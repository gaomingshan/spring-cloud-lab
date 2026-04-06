package com.lab.feign.consumer.client;

import com.lab.common.result.Result;
import com.lab.feign.consumer.client.fallback.ProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign 客户端接口
 *
 * @FeignClient 属性说明：
 *   name/value  : 目标服务名（与 Nacos 注册服务名一致）
 *   path        : 公共路径前缀（可选）
 *   fallback    : 降级实现类（服务不可用时调用）
 *   url         : 直连 URL（跳过负载均衡，测试用）
 */
@FeignClient(
        name = "feign-provider",
        path = "/provider",
        fallback = ProviderFallback.class
)
public interface ProviderClient {

    /**
     * 调用 provider 的 hello 接口
     * Feign 自动将方法签名映射为 HTTP 请求
     */
    @GetMapping("/hello")
    Result<Map<String, Object>> hello();

    /**
     * 路径参数传递
     */
    @GetMapping("/user/{id}")
    Result<Map<String, Object>> getUser(@PathVariable("id") Long id);

    /**
     * 请求体传递（POST）
     */
    @PostMapping("/order")
    Result<String> createOrder(@RequestBody Map<String, Object> body);
}
