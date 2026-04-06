package com.lab.stream.consumer.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 消息消费者（Spring Cloud Stream 函数式编程模型）
 *
 * Bean 方法名与 application.yml function.definition 中的名称对应：
 *   orderConsumer  -> bindings: order-in-0
 *   delayConsumer  -> bindings: delay-in-0
 */
@Slf4j
@Component
public class MessageConsumer {

    /**
     * 普通消息消费
     * 消费失败会自动重试（默认3次），超过重试次数进入死信队列（DLQ）
     */
    @Bean
    public Consumer<String> orderConsumer() {
        return message -> {
            log.info("[Consumer] 收到 ORDER 消息: {}", message);
            // 模拟消费失败（取消注释测试重试和 DLQ）
            // if (message.contains("fail")) {
            //     throw new RuntimeException("模拟消费失败，触发重试");
            // }
        };
    }

    /**
     * 延迟消息消费
     * 消息在 producer 发送后，经过指定延迟时间才会被投递到此处
     */
    @Bean
    public Consumer<String> delayConsumer() {
        return message -> {
            log.info("[Consumer] 收到 DELAY 消息（已延迟投递）: {}", message);
        };
    }
}
