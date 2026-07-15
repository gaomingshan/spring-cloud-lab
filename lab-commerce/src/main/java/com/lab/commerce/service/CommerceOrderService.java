package com.lab.commerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.common.exception.BizException;
import com.lab.commerce.client.AccountClient;
import com.lab.commerce.client.StorageClient;
import com.lab.commerce.controller.CommerceOrderController.CreateOrderRequest;
import com.lab.commerce.dto.CommerceOrderView;
import com.lab.commerce.dto.OutboxEventView;
import com.lab.commerce.entity.CommerceOrder;
import com.lab.commerce.entity.OutboxEvent;
import com.lab.commerce.mapper.CommerceOrderMapper;
import com.lab.commerce.mapper.OutboxEventMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommerceOrderService {

    private final CommerceOrderMapper commerceOrderMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final AccountClient accountClient;
    private final StorageClient storageClient;
    private final ObjectMapper objectMapper;

    @GlobalTransactional(name = "commerce-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public CommerceOrderView create(CreateOrderRequest request) {
        return createInternal(request, false);
    }

    @GlobalTransactional(name = "commerce-create-order-fail", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public CommerceOrderView createAndRollback(CreateOrderRequest request) {
        return createInternal(request, true);
    }

    private CommerceOrderView createInternal(CreateOrderRequest request, boolean forceRollback) {
        if (commerceOrderMapper.reserve(request.requestId(), request.userId(), request.productId(), request.count(), request.money()) == 0) {
            return find(request.requestId());
        }

        storageClient.decrease(request.productId(), request.count());
        accountClient.decrease(request.userId(), request.money());

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("ORDER");
        event.setAggregateId(request.requestId());
        event.setEventType("ORDER_CREATED");
        event.setPayload(eventPayload(request));
        event.setStatus("PENDING");
        event.setRetryCount(0);
        outboxEventMapper.insert(event);
        commerceOrderMapper.markCreated(request.requestId());

        if (forceRollback) {
            throw BizException.of("故障注入：验证 Seata 全局回滚与 Outbox 原子性");
        }
        return find(request.requestId());
    }

    public CommerceOrderView find(String requestId) {
        CommerceOrder order = commerceOrderMapper.selectByRequestId(requestId);
        if (order == null) {
            throw BizException.of("订单不存在");
        }
        List<OutboxEventView> events = outboxEventMapper.selectByAggregateId(requestId).stream()
                .map(event -> new OutboxEventView(event.getId(), event.getEventType(), event.getStatus(),
                        event.getRetryCount(), event.getCreatedAt(), event.getPublishedAt()))
                .toList();
        return new CommerceOrderView(order.getId(), order.getRequestId(), order.getUserId(), order.getProductId(),
                order.getCount(), order.getMoney(), order.getStatus(), order.getCreatedAt(), events);
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
