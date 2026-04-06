package com.lab.seata.order.controller;

import com.lab.common.result.Result;
import com.lab.seata.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单接口（触发分布式事务）
     * AT 模式：成功则三个库全部提交，失败则全部回滚
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param count     数量
     * @param money     金额
     */
    @PostMapping("/create")
    public Result<String> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer count,
            @RequestParam BigDecimal money) {
        orderService.createOrder(userId, productId, count, money);
        return Result.ok("下单成功");
    }

    /**
     * 模拟失败场景（验证回滚）
     * 传入 rollback=true，在事务中途抛出异常
     */
    @PostMapping("/create-fail")
    public Result<String> createOrderWithFail(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer count,
            @RequestParam BigDecimal money) {
        orderService.createOrderWithFail(userId, productId, count, money);
        return Result.ok("不会到达这里");
    }
}
