package com.lab.commerce.controller;

import com.lab.common.result.Result;
import com.lab.commerce.dto.CommerceOrderView;
import com.lab.commerce.service.CommerceOrderService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/commerce/orders")
public class CommerceOrderController {

    private final CommerceOrderService commerceOrderService;

    @PostMapping
    public Result<CommerceOrderView> create(@Validated @RequestBody CreateOrderRequest request) {
        return Result.ok(commerceOrderService.create(request));
    }

    @PostMapping("/fail")
    public Result<CommerceOrderView> createAndRollback(@Validated @RequestBody CreateOrderRequest request) {
        return Result.ok(commerceOrderService.createAndRollback(request));
    }

    @GetMapping("/{requestId}")
    public Result<CommerceOrderView> find(@PathVariable String requestId) {
        return Result.ok(commerceOrderService.find(requestId));
    }

    public record CreateOrderRequest(
            @NotBlank String requestId,
            @NotNull Long userId,
            @NotNull Long productId,
            @NotNull @Min(1) Integer count,
            @NotNull @DecimalMin(value = "0.01") BigDecimal money) {
    }
}
