package com.lab.seata.account.controller;

import com.lab.common.result.Result;
import com.lab.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 扣减账户余额（AT 模式参与方）
     * Seata 通过 XID（全局事务ID）将此本地事务纳入全局事务管理
     * AT 模式：本地事务提交前，Seata 自动记录 undo_log，TC 回滚时通过 undo_log 补偿
     */
    @PostMapping("/decrease")
    @Transactional
    public Result<Void> decrease(@RequestParam Long userId, @RequestParam BigDecimal money) {
        log.info("[Account] 扣减余额: userId={}, money={}", userId, money);
        int updated = jdbcTemplate.update(
                "UPDATE t_account SET used = used + ?, residue = residue - ? "
                        + "WHERE user_id = ? AND residue >= ?",
                money, money, userId, money);
        if (updated != 1) {
            throw BizException.of("账户不存在或可用余额不足");
        }
        log.info("[Account] 扣减余额成功");
        return Result.ok();
    }
}
