package com.lab.message.lab.event.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateModel(String orderId, List<String> skuIds, BigDecimal amount) {

}
