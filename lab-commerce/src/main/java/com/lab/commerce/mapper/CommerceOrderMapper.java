package com.lab.commerce.mapper;

import com.lab.commerce.entity.CommerceOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface CommerceOrderMapper extends BaseMapper<CommerceOrder> {

    int reserve(@Param("requestId") String requestId, @Param("userId") Long userId,
                @Param("productId") Long productId, @Param("count") Integer count,
                @Param("money") BigDecimal money);

    CommerceOrder selectByRequestId(@Param("requestId") String requestId);

    int markCreated(@Param("requestId") String requestId);
}
