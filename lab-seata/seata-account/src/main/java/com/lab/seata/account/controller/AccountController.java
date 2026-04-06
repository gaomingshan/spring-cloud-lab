package com.lab.seata.account.controller;

import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    /**
     * 扣减账户余额（AT 模式参与方）
     * Seata 通过 XID（全局事务ID）将此本地事务纳入全局事务管理
     * AT 模式：本地事务提交前，Seata 自动记录 undo_log，TC 回滚时通过 undo_log 补偿
     */
    @PostMapping("/decrease")
    @Transactional
    public Result<Void> decrease(@RequestParam Long userId,
                                  @RequestParam BigDecimal money) {
        log.info("[Account] 扣减余额: userId={}, money={}", userId, money);
        // accountMapper.decrease(userId, money);
        // 模拟账户余额不足异常（测试回滚）
        // if (money.compareTo(new BigDecimal("9999")) > 0) {
        //     throw new BizException("余额不足");
        // }
        log.info("[Account] 扣减余额成功");
        return Result.ok();
    }
}
