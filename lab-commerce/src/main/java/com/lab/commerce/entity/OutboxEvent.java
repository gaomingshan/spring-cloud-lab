package com.lab.commerce.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("t_outbox_event")
public class OutboxEvent {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private String status;
    private Integer retryCount;
    private String processingToken;
    private LocalDateTime processingUntil;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
