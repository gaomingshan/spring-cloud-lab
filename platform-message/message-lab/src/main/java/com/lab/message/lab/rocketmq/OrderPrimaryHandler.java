package com.lab.message.lab.rocketmq;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.lab.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EventConsumer(topic = "lab_order_created_events", group = "message-lab-order-primary")
@Profile({"rocketmq", "kafka"})
public class OrderPrimaryHandler implements EventHandler<OrderCreatedEvent> {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("[message][consume][primary] eventId={} orderId={} amount={} thread={}",
                event.getEventId(), event.getOrder().orderId(), event.getOrder().amount(), Thread.currentThread().getName());
        jdbcTemplate.update("""
                insert into reliable_message_lab_effect (event_id, consumer_name, created_at)
                values (?, 'primary', utc_timestamp(6))
                """, event.getEventId());
    }
}
