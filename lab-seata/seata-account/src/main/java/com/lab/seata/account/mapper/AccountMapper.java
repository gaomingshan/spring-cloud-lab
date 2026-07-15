package com.lab.seata.account.mapper;

import com.lab.seata.account.entity.Account;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    int decrease(@Param("userId") Long userId, @Param("money") BigDecimal money);
}
