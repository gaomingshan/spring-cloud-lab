package com.lab.sharding.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分库分表演示 Controller
 * 观察控制台 SQL 路由日志（sql-show=true），了解 ShardingSphere 路由原理
 */
@Slf4j
@RestController
@RequestMapping("/sharding")
public class ShardingController {

    /**
     * 插入订单（观察路由到哪个库哪张表）
     * user_id % 2 决定库，order_id % 2 决定表
     * 例：userId=1,orderId=奇数 → ds1.t_order_1
     */
    @PostMapping("/order")
    public Result<String> insertOrder(
            @RequestParam Long userId,
            @RequestParam(required = false) Long orderId) {
        log.info("[Sharding] 插入订单: userId={}, orderId={}", userId, orderId);
        // orderMapper.insert(new Order(orderId, userId, ...));
        // 控制台会打印：Logic SQL + Actual SQL（路由后的物理SQL）
        return Result.ok("插入成功，观察控制台路由日志");
    }

    /**
     * 精确查询（带分片键 user_id + order_id，精确路由到单表）
     */
    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> queryOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        log.info("[Sharding] 精确查询: userId={}, orderId={}", userId, orderId);
        // 精确路由：只查 ds{userId%2}.t_order_{orderId%2}
        return Result.ok(Map.of("orderId", orderId, "userId", userId,
                "tip", "观察控制台：精确路由只查1张表"));
    }

    /**
     * 范围查询（只带 user_id，需扫描该库下所有分表）
     */
    @GetMapping("/orders")
    public Result<Object> queryByUser(@RequestParam Long userId) {
        log.info("[Sharding] 按userId查询: userId={}", userId);
        // 路由到 ds{userId%2} 下所有分表（全表扫描该库）
        return Result.ok(Map.of("userId", userId,
                "tip", "观察控制台：路由到指定库的所有分表"));
    }
}
