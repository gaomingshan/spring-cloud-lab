package com.lab.seata.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务 - 全局事务发起方
 *
 * 演示场景：下单扣库存 + 扣账户余额，任一失败全局回滚
 *   1. AT 模式：@GlobalTransactional 自动管理，无需改业务代码
 *   2. TCC 模式：手动实现 Try/Confirm/Cancel 三阶段
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SeataOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeataOrderApplication.class, args);
    }
}
