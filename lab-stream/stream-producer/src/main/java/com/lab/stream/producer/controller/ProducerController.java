package com.lab.stream.producer.controller;

import com.lab.common.result.Result;
import com.lab.stream.producer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息发送演示 Controller
 */
@RestController
@RequestMapping("/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final MessageService messageService;

    /** 发送普通消息 */
    @PostMapping("/send")
    public Result<String> send(@RequestBody Map<String, Object> body) {
        messageService.sendNormal(body);
        return Result.ok("消息发送成功");
    }

    /** 发送延迟消息（RocketMQ 延迟级别 1~18） */
    @PostMapping("/send-delay")
    public Result<String> sendDelay(
            @RequestBody Map<String, Object> body,
            @RequestParam(defaultValue = "3") int delayLevel) {
        messageService.sendDelay(body, delayLevel);
        return Result.ok("延迟消息发送成功，级别: " + delayLevel);
    }

    /** 发送顺序消息（同一 shardingKey 路由到同一 Queue） */
    @PostMapping("/send-ordered")
    public Result<String> sendOrdered(
            @RequestBody Map<String, Object> body,
            @RequestParam String orderId) {
        messageService.sendOrdered(body, orderId);
        return Result.ok("顺序消息发送成功");
    }
}
