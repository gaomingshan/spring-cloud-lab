package com.lab.feign.consumer.controller;

import com.lab.common.result.Result;
import com.lab.feign.consumer.client.ProviderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign 消费方 Controller
 * 通过 ProviderClient（Feign 代理）调用 feign-provider 服务
 */
@Slf4j
@RestController
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class FeignConsumerController {

    private final ProviderClient providerClient;

    /**
     * 调用 provider hello 接口
     * 多次请求观察返回的 port 交替变化（LoadBalancer 轮询）
     */
    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        return providerClient.hello();
    }

    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> getUser(@PathVariable Long id) {
        return providerClient.getUser(id);
    }

    @PostMapping("/order")
    public Result<String> createOrder(@RequestBody Map<String, Object> body) {
        return providerClient.createOrder(body);
    }
}
