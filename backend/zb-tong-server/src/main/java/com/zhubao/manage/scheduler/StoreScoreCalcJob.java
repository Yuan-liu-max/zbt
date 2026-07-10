package com.zhubao.manage.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.task.entity.TaskAudit;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskAuditMapper;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 门店月度评分Job —— 每月1日5:00执行
 *
 * 调度配置: cron: 0 0 5 1 * ?
 *
 * 计算维度:
 *   人效分(35%) → TODO: 汇总 employee_monthly_review 数据
 *   货品分(30%) → TODO: 汇总 product_inventory_check / product_sales_analysis
 *   场景分(25%) → TODO: 汇总 scene_* 检查表得分
 *   纪律分(10%) → 超时次数 + 驳回次数
 *
 * 结果写入 store_monthly_score
 */
@Component
public class StoreScoreCalcJob {

    private static final Logger log = LoggerFactory.getLogger(StoreScoreCalcJob.class);

    private final StoreMapper storeMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskAuditMapper taskAuditMapper;

    public StoreScoreCalcJob(StoreMapper storeMapper, TaskInstanceMapper taskInstanceMapper,
                             TaskAuditMapper taskAuditMapper) {
        this.storeMapper = storeMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskAuditMapper = taskAuditMapper;
    }

    @XxlJob("storeScoreCalcHandler")
    public void storeScoreCalcHandler() {
        String scoreMonth = LocalDate.now().minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("===== 门店月度评分开始 month={} =====", scoreMonth);
        int count = 0;
        try {
            List<Store> stores = storeMapper.selectList(
                    new LambdaQueryWrapper<Store>().eq(Store::getStatus, "OPEN"));

            for (Store store : stores) {
                calcStoreScore(store.getId(), scoreMonth);
                count++;
            }
            String msg = "门店评分完成，已计算 " + count + " 家门店 (" + scoreMonth + ")";
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("门店评分计算失败", e);
            XxlJobHelper.handleFail("门店评分计算失败: " + e.getMessage());
        }
    }

    /**
     * 计算单个门店月度评分
     *
     * TODO: 后续由 ScoreCalcService 承接完整评分引擎逻辑
     */
    private void calcStoreScore(Long storeId, String scoreMonth) {
        // ---- 纪律分（10%）基于任务超时和驳回 ----
        long overdueCount = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getStoreId, storeId)
                        .eq(TaskInstance::getIsOverdue, 1)
                        .ge(TaskInstance::getCreatedAt, scoreMonth + "-01")
                        .lt(TaskInstance::getCreatedAt, nextMonth(scoreMonth) + "-01"));

        // 驳回次数 = 查询 task_audit 表 audit_result = 'REJECTED'
        long rejectedCount = taskAuditMapper.selectCount(
                new LambdaQueryWrapper<TaskAudit>()
                        .eq(TaskAudit::getAuditResult, "REJECTED")
                        .ge(TaskAudit::getAuditedAt, scoreMonth + "-01")
                        .lt(TaskAudit::getAuditedAt, nextMonth(scoreMonth) + "-01")
                        .apply("task_id IN (SELECT id FROM task_instance WHERE store_id = {0})", storeId));

        // 纪律分：满分10分，每次超时-0.5，每次驳回-0.5，最低0分
        BigDecimal disciplineScore = BigDecimal.valueOf(Math.max(0, 10 - overdueCount * 0.5 - rejectedCount * 0.5));

        // ---- 人效分(35%) / 货品分(30%) / 场景分(25%) 暂用默认值 ----
        // TODO: 接入 HumanService / ProductService / SceneService 的汇总数据
        BigDecimal humanScore = BigDecimal.valueOf(35);
        BigDecimal productScore = BigDecimal.valueOf(30);
        BigDecimal sceneScore = BigDecimal.valueOf(25);

        BigDecimal totalScore = humanScore.add(productScore).add(sceneScore).add(disciplineScore)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("门店[{}]评分: 总分={}, 人效={}, 货品={}, 场景={}, 纪律={}",
                storeId, totalScore, humanScore, productScore, sceneScore, disciplineScore);

        // TODO: 写入 store_monthly_score 表（开发 report 模块时接入 StoreMonthlyScoreMapper）
    }

    private String nextMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4));
        int m = Integer.parseInt(ym.substring(5, 7));
        if (m == 12) return (y + 1) + "-01";
        return y + "-" + String.format("%02d", m + 1);
    }
}
