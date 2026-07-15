package com.lab.seata.order.mapper;

import com.lab.seata.order.entity.Order;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
