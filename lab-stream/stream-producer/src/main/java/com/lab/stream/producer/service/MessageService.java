package com.lab.stream.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 消息发送服务
 * 使用 RocketMQTemplate 直接发送（比 Stream Binder 更灵活）
 * Stream Binder 方式适合函数式编程模型，见 application.yml function.definition
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    /** 普通消息 */
    @SneakyThrows
    public void sendNormal(Map<String, Object> body) {
        String json = objectMapper.writeValueAsString(body);
        log.info("[Producer] 发送普通消息: {}", json);
        rocketMQTemplate.convertAndSend("ORDER_TOPIC", json);
    }

    /**
     * 延迟消息
     * RocketMQ 延迟级别：1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h
     * delayLevel=3 表示 10s 后投递
     */
    @SneakyThrows
    public void sendDelay(Map<String, Object> body, int delayLevel) {
        String json = objectMapper.writeValueAsString(body);
        log.info("[Producer] 发送延迟消息: level={}, body={}", delayLevel, json);
        rocketMQTemplate.syncSend(
                "DELAY_TOPIC",
                MessageBuilder.withPayload(json).build(),
                3000,
                delayLevel
        );
    }

    /**
     * 顺序消息
     * 相同 hashKey（如 orderId）的消息路由到同一 Queue，保证消费顺序
     */
    @SneakyThrows
    public void sendOrdered(Map<String, Object> body, String hashKey) {
        String json = objectMapper.writeValueAsString(body);
        log.info("[Producer] 发送顺序消息: hashKey={}, body={}", hashKey, json);
        rocketMQTemplate.syncSendOrderly("ORDER_TOPIC", json, hashKey);
    }
}
