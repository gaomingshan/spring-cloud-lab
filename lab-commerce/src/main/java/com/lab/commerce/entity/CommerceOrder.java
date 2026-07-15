package com.lab.commerce.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("t_commerce_order")
public class CommerceOrder {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private String requestId;
    private Long userId;
    private Long productId;
    private Integer count;
    private BigDecimal money;
    private String status;
    private LocalDateTime createdAt;
}
