package com.lab.multids.mongodb.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为日志（存 MongoDB）
 * 适合 MongoDB 的场景：高写入频率、Schema 灵活、无需强事务
 */
@Data
@Document(collection = "user_behavior_log")
public class UserBehaviorLog {

    @Id
    private String id;

    @Indexed
    private Long userId;

    /** 行为类型：LOGIN/CLICK/PURCHASE/VIEW */
    private String action;

    /** 灵活扩展字段（MongoDB 文档型优势） */
    private Map<String, Object> extra;

    private LocalDateTime createTime;
}
