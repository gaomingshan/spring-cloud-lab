package com.lab.seata.storage.mapper;

import com.lab.seata.storage.entity.Storage;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StorageMapper extends BaseMapper<Storage> {

    int decrease(@Param("productId") Long productId, @Param("count") Integer count);
}
