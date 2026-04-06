package com.lab.feign.provider.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/provider")
public class FeignProviderController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/hello")
    public Result<Map<String, Object>> hello(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("[Provider:{}] hello, traceId={}, userId={}", port, traceId, userId);
        return Result.ok(Map.of(
                "port", port,
                "traceId", traceId != null ? traceId : "",
                "message", "Hello from feign-provider!"
        ));
    }

    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> getUser(@PathVariable Long id) {
        log.info("[Provider:{}] getUser id={}", port, id);
        return Result.ok(Map.of(
                "id", id,
                "name", "User-" + id,
                "port", port
        ));
    }

    @PostMapping("/order")
    public Result<String> createOrder(@RequestBody Map<String, Object> body) {
        log.info("[Provider:{}] createOrder body={}", port, body);
        return Result.ok("ORDER-" + System.currentTimeMillis());
    }
}
