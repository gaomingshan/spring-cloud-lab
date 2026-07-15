package com.lab.seata.order.service;

import com.lab.seata.order.client.AccountClient;
import com.lab.seata.order.client.StorageClient;
import com.lab.seata.order.entity.Order;
import com.lab.seata.order.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final AccountClient accountClient;
    private final StorageClient storageClient;
    private final OrderMapper orderMapper;

    /**
     * AT 模式分布式事务
     * @GlobalTransactional 开启全局事务，XID 自动透传到 account/storage 服务
     * 任何一步失败，Seata TC 协调所有参与方回滚（通过 undo_log）
     */
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    @Transactional
    public void createOrder(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("[Order] 开始下单: userId={}, productId={}, count={}, money={}", userId, productId, count, money);

        // 1. 扣减库存（调用 seata-storage 服务）
        log.info("[Order] 步骤1: 扣减库存");
        storageClient.decrease(productId, count);

        // 2. 扣减账户余额（调用 seata-account 服务）
        log.info("[Order] 步骤2: 扣减账户余额");
        accountClient.decrease(userId, money);

        // 3. 本地保存订单，作为 Seata AT 的一个分支事务。
        log.info("[Order] 步骤3: 创建订单记录");
        orderMapper.insert(newOrder(userId, productId, count, money, 1));

        log.info("[Order] 下单成功，全局事务提交");
    }

    /**
     * 模拟失败场景：步骤2后抛异常，验证 Seata 回滚步骤1和步骤2
     */
    @GlobalTransactional(name = "create-order-fail", rollbackFor = Exception.class)
    @Transactional
    public void createOrderWithFail(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("[Order-Fail] 开始下单（将在中途失败）");

        storageClient.decrease(productId, count);
        accountClient.decrease(userId, money);
        orderMapper.insert(newOrder(userId, productId, count, money, 0));

        // 模拟异常 → 触发全局回滚
        log.info("[Order-Fail] 模拟业务异常，触发全局回滚...");
        throw new RuntimeException("模拟异常：触发 Seata 全局回滚");
    }

    private Order newOrder(Long userId, Long productId, Integer count, BigDecimal money, int status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(status);
        return order;
    }
}
