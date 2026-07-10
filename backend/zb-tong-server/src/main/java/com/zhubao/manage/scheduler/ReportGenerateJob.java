package com.zhubao.manage.scheduler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 月度报表生成Job —— 每月1日6:00执行
 *
 * 调度配置: cron: 0 0 6 1 * ?
 *
 * 逻辑:
 *   1. 门店综合评分排名报表
 *   2. 员工绩效排名报表
 *   3. 任务执行汇总报表（完成率/超时率/驳回率）
 *   4. 销售数据汇总报表
 *   5. 写入 report_snapshot 表(JSON)
 *
 * TODO: 开发 ReportService 后接入
 */
@Component
public class ReportGenerateJob {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerateJob.class);

    @XxlJob("reportGenerateHandler")
    public void reportGenerateHandler() {
        String period = LocalDate.now().minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("===== 月度报表生成开始 period={} =====", period);

        try {
            int total = 0;

            // TODO: 门店评分排名报表
            // reportService.generateStoreRankingReport(period);
            log.info("[报表1/4] 门店排名报表 - TODO: ReportService.generateStoreRankingReport({})", period);

            // TODO: 员工绩效排名报表
            // reportService.generateEmployeePerformanceReport(period);
            log.info("[报表2/4] 员工绩效报表 - TODO: ReportService.generateEmployeePerformanceReport({})", period);

            // TODO: 任务执行汇总报表
            // reportService.generateTaskSummaryReport(period);
            log.info("[报表3/4] 任务汇总报表 - TODO: ReportService.generateTaskSummaryReport({})", period);

            // TODO: 销售数据汇总报表
            // reportService.generateSalesSummaryReport(period);
            log.info("[报表4/4] 销售汇总报表 - TODO: ReportService.generateSalesSummaryReport({})", period);

            String msg = "月度报表生成完成 (" + period + ")，已生成 " + total + " 份快照";
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("月度报表生成失败", e);
            XxlJobHelper.handleFail("月度报表生成失败: " + e.getMessage());
        }
    }
}
