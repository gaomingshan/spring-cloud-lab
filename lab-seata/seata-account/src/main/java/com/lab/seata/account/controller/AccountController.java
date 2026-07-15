package com.lab.seata.account.controller;

import com.lab.common.result.Result;
import com.lab.seata.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    /**
     * 扣减账户余额（AT 模式参与方）
     * Seata 通过 XID（全局事务ID）将此本地事务纳入全局事务管理
     * AT 模式：本地事务提交前，Seata 自动记录 undo_log，TC 回滚时通过 undo_log 补偿
     */
    @PostMapping("/decrease")
    public Result<Void> decrease(@RequestParam Long userId, @RequestParam BigDecimal money) {
        accountService.decrease(userId, money);
        return Result.ok();
    }
}
