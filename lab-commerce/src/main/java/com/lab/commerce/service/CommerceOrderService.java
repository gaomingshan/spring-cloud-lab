package com.lab.commerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.common.exception.BizException;
import com.lab.commerce.client.AccountClient;
import com.lab.commerce.client.StorageClient;
import com.lab.commerce.controller.CommerceOrderController.CreateOrderRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommerceOrderService {

    private final JdbcTemplate jdbcTemplate;
    private final AccountClient accountClient;
    private final StorageClient storageClient;
    private final ObjectMapper objectMapper;
    private final StreamBridge streamBridge;

    @GlobalTransactional(name = "commerce-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(CreateOrderRequest request) {
        return createInternal(request, false);
    }

    @GlobalTransactional(name = "commerce-create-order-fail", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAndRollback(CreateOrderRequest request) {
        return createInternal(request, true);
    }

    private Map<String, Object> createInternal(CreateOrderRequest request, boolean forceRollback) {
        Map<String, Object> existing = findOptional(request.requestId());
        if (existing != null) {
            return existing;
        }

        storageClient.decrease(request.productId(), request.count());
        accountClient.decrease(request.userId(), request.money());

        try {
            jdbcTemplate.update("INSERT INTO t_commerce_order (request_id, user_id, product_id, count, money, status) "
                            + "VALUES (?, ?, ?, ?, ?, 'CREATED')",
                    request.requestId(), request.userId(), request.productId(), request.count(), request.money());
        } catch (DuplicateKeyException exception) {
            return find(request.requestId());
        }

        jdbcTemplate.update("INSERT INTO t_outbox_event (aggregate_type, aggregate_id, event_type, payload) "
                        + "VALUES ('ORDER', ?, 'ORDER_CREATED', ?)",
                request.requestId(), eventPayload(request));

        if (forceRollback) {
            throw BizException.of("故障注入：验证 Seata 全局回滚与 Outbox 原子性");
        }
        return find(request.requestId());
    }

    public Map<String, Object> find(String requestId) {
        Map<String, Object> order = findOptional(requestId);
        if (order == null) {
            throw BizException.of("订单不存在");
        }
        List<Map<String, Object>> events = jdbcTemplate.queryForList(
                "SELECT id, event_type, status, retry_count, created_at, published_at "
                        + "FROM t_outbox_event WHERE aggregate_id = ? ORDER BY id", requestId);
        order.put("outboxEvents", events);
        return order;
    }

    @Scheduled(fixedDelayString = "${commerce.outbox.publish-delay-ms:2000}")
    public void publishPendingEvents() {
        List<Map<String, Object>> events = jdbcTemplate.queryForList(
                "SELECT id, payload FROM t_outbox_event WHERE status = 'PENDING' ORDER BY id LIMIT 50");
        for (Map<String, Object> event : events) {
            Long id = ((Number) event.get("id")).longValue();
            try {
                boolean sent = streamBridge.send("orderEvent-out-0", event.get("payload"));
                if (!sent) {
                    throw new IllegalStateException("RocketMQ Binder rejected the event");
                }
                jdbcTemplate.update("UPDATE t_outbox_event SET status = 'PUBLISHED', published_at = NOW() WHERE id = ?", id);
            } catch (Exception exception) {
                jdbcTemplate.update("UPDATE t_outbox_event SET retry_count = retry_count + 1 WHERE id = ?", id);
                log.warn("Outbox event {} was not published and will be retried", id, exception);
            }
        }
    }

    private Map<String, Object> findOptional(String requestId) {
        List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT id, request_id, user_id, product_id, count, money, status, created_at "
                        + "FROM t_commerce_order WHERE request_id = ?", requestId);
        return orders.isEmpty() ? null : new LinkedHashMap<>(orders.get(0));
    }

    private String eventPayload(CreateOrderRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "requestId", request.requestId(),
                    "userId", request.userId(),
                    "productId", request.productId(),
                    "count", request.count(),
                    "money", request.money()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize outbox event", exception);
        }
    }
}
