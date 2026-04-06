package com.lab.xxljob.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XXL-Job Handler 集合
 *
 * 在 XXL-Job Admin 控制台创建任务时，JobHandler 填写此处方法上的 @XxlJob value
 */
@Slf4j
@Component
public class DemoJobHandler {

    /**
     * 简单任务：Bean 模式
     * Admin 中配置：JobHandler=simpleJob，Cron=0/5 * * * * ?
     */
    @XxlJob("simpleJob")
    public void simpleJob() {
        log.info("[XXL-Job] simpleJob 执行，jobId={}", XxlJobHelper.getJobId());
        // 业务逻辑：如清理过期数据、生成报表等
        XxlJobHelper.log("simpleJob 执行成功");
    }

    /**
     * 分片广播任务
     * 多个执行器实例各自处理一部分数据，实现并行化
     *
     * 场景：100万条数据，3个执行器实例，每个处理约33万条
     * Admin 配置：路由策略=分片广播
     */
    @XxlJob("shardingJob")
    public void shardingJob() {
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  // 当前分片索引（0/1/2...）
        int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数
        log.info("[XXL-Job] shardingJob 执行: shardIndex={}, shardTotal={}", shardIndex, shardTotal);

        // 按分片处理数据（如 WHERE id % shardTotal = shardIndex）
        int pageSize = 1000;
        int page = 0;
        while (true) {
            // List<Data> list = dataService.queryBySharding(shardIndex, shardTotal, page, pageSize);
            // if (list.isEmpty()) break;
            // processData(list);
            // page++;
            log.info("[XXL-Job] 分片 {}/{} 处理第{}页数据", shardIndex, shardTotal, page);
            break; // 演示：实际应循环处理
        }
        XxlJobHelper.log("shardingJob 分片 {}/{} 执行完成", shardIndex, shardTotal);
    }

    /**
     * 带参数任务（通过 Admin 传入 JobParam）
     */
    @XxlJob("paramJob")
    public void paramJob() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("[XXL-Job] paramJob 执行，param={}", jobParam);
        // 解析参数执行不同逻辑
        XxlJobHelper.log("paramJob 执行完成，param={}", jobParam);
    }
}
