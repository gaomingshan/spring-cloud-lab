package com.lab.seata.storage.controller;

import com.lab.common.result.Result;
import com.lab.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 扣减库存（AT 模式参与方）
     * Seata AT 模式全自动：
     *   1. 执行前，Seata 拦截 SQL，查询并保存 before image（undo_log）
     *   2. 执行本地 SQL：UPDATE storage SET used=used+count WHERE product_id=?
     *   3. 查询 after image
     *   4. 全局事务提交：删除 undo_log，释放本地锁
     *   5. 全局事务回滚：用 undo_log 生成反向 SQL 执行补偿
     */
    @PostMapping("/decrease")
    @Transactional
    public Result<Void> decrease(@RequestParam Long productId,
                                    @RequestParam Integer count) {
        log.info("[Storage] 扣减库存: productId={}, count={}", productId, count);
        int updated = jdbcTemplate.update(
                "UPDATE t_storage SET used = used + ?, residue = residue - ? "
                        + "WHERE product_id = ? AND residue >= ?",
                count, count, productId, count);
        if (updated != 1) {
            throw BizException.of("商品不存在或可用库存不足");
        }
        log.info("[Storage] 扣减库存成功");
        return Result.ok();
    }
}
