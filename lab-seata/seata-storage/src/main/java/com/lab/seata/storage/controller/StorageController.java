package com.lab.seata.storage.controller;

import com.lab.common.result.Result;
import com.lab.seata.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

    private final StorageService storageService;

    /**
     * 扣减库存（AT 模式参与方）
     * Seata AT 模式全自动：
     *   1. 执行前，Seata 拦截 SQL，查询并保存 before image（undo_log）
     *   2. 执行库存扣减 Mapper，Seata 数据源代理记录 before/after image
     *   3. 查询 after image
     *   4. 全局事务提交：删除 undo_log，释放本地锁
     *   5. 全局事务回滚：用 undo_log 生成反向 SQL 执行补偿
     */
    @PostMapping("/decrease")
    public Result<Void> decrease(@RequestParam Long productId,
                                    @RequestParam Integer count) {
        storageService.decrease(productId, count);
        return Result.ok();
    }
}
