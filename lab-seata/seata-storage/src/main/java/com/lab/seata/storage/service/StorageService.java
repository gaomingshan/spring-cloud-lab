package com.lab.seata.storage.service;

import com.lab.common.exception.BizException;
import com.lab.seata.storage.mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageMapper storageMapper;

    @Transactional(rollbackFor = Exception.class)
    public void decrease(Long productId, Integer count) {
        log.info("[Storage] 扣减库存: productId={}, count={}", productId, count);
        if (storageMapper.decrease(productId, count) != 1) {
            throw BizException.of("商品不存在或可用库存不足");
        }
        log.info("[Storage] 扣减库存成功");
    }
}
