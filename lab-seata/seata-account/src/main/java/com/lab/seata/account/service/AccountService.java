package com.lab.seata.account.service;

import com.lab.common.exception.BizException;
import com.lab.seata.account.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;

    @Transactional(rollbackFor = Exception.class)
    public void decrease(Long userId, BigDecimal money) {
        log.info("[Account] 扣减余额: userId={}, money={}", userId, money);
        if (accountMapper.decrease(userId, money) != 1) {
            throw BizException.of("账户不存在或可用余额不足");
        }
        log.info("[Account] 扣减余额成功");
    }
}
