package com.lab.registry.provider.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 服务提供方接口
 * consumer 通过服务名发现并调用此接口，观察负载均衡效果
 */
@Slf4j
@RestController
@RequestMapping("/provider")
public class ProviderController {

    /** 注入当前实例端口，用于区分多个实例的响应 */
    @Value("${server.port}")
    private int port;

    @Value("${spring.application.name}")
    private String appName;

    /**
     * 基础问候接口
     * 多实例时，consumer 轮询调用会看到不同的 port，证明负载均衡生效
     */
    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        log.info("[Provider] 收到请求，当前实例端口: {}", port);
        return Result.ok(Map.of(
                "message", "Hello from provider!",
                "instancePort", port,
                "serviceName", appName
        ));
    }

    /**
     * 带参数接口，演示服务发现后的正常业务调用
     */
    @GetMapping("/echo/{msg}")
    public Result<String> echo(@PathVariable String msg) {
        log.info("[Provider:{}] echo: {}", port, msg);
        return Result.ok(String.format("[Port:%d] echo: %s", port, msg));
    }
}
