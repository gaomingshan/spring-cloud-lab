package com.lab.commerce.service;

import com.lab.commerce.entity.OutboxEvent;
import com.lab.commerce.mapper.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final OutboxEventMapper outboxEventMapper;
    private final StreamBridge streamBridge;

    @Scheduled(fixedDelayString = "${commerce.outbox.publish-delay-ms:2000}")
    public void publishPendingEvents() {
        for (OutboxEvent event : outboxEventMapper.selectDispatchCandidates(50)) {
            String token = UUID.randomUUID().toString();
            if (outboxEventMapper.claim(event.getId(), token, LocalDateTime.now().plusMinutes(1)) != 1) {
                continue;
            }
            try {
                if (!streamBridge.send("orderEvent-out-0", event.getPayload())) {
                    throw new IllegalStateException("RocketMQ Binder rejected the event");
                }
                outboxEventMapper.markPublished(event.getId(), token);
            } catch (Exception exception) {
                outboxEventMapper.releaseForRetry(event.getId(), token);
                log.warn("Outbox event {} was not published and will be retried", event.getId(), exception);
            }
        }
    }
}
