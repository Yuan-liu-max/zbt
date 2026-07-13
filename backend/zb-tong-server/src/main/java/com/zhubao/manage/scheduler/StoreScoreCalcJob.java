package com.zhubao.manage.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.human.entity.EmployeeAssessment;
import com.zhubao.manage.module.human.mapper.EmployeeAssessmentMapper;

import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.product.entity.ProductInventoryCheck;
import com.zhubao.manage.module.product.mapper.ProductInventoryCheckMapper;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.mapper.StoreMonthlyScoreMapper;
import com.zhubao.manage.module.scene.entity.SceneHealthInspection;
import com.zhubao.manage.module.scene.mapper.SceneHealthInspectionMapper;
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
 * 评分公式 (100分制):
 *   人效分(35) = 本月员工考核平均分 / 100 * 35
 *   货品分(30) = (1 - 盘点异常率) * 30
 *   场景分(25) = (1 - 卫生需整改率) * 25
 *   纪律分(10) = 10 - 超时×0.5 - 驳回×0.5，最低0
 */
@Component
public class StoreScoreCalcJob {

    private static final Logger log = LoggerFactory.getLogger(StoreScoreCalcJob.class);

    private final StoreMapper storeMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskAuditMapper taskAuditMapper;
    private final EmployeeAssessmentMapper assessmentMapper;
    private final ProductInventoryCheckMapper inventoryCheckMapper;
    private final SceneHealthInspectionMapper healthMapper;
    private final StoreMonthlyScoreMapper scoreMapper;

    public StoreScoreCalcJob(StoreMapper storeMapper, TaskInstanceMapper taskInstanceMapper,
                             TaskAuditMapper taskAuditMapper,
                             EmployeeAssessmentMapper assessmentMapper,
                             ProductInventoryCheckMapper inventoryCheckMapper,
                             SceneHealthInspectionMapper healthMapper,
                             StoreMonthlyScoreMapper scoreMapper) {
        this.storeMapper = storeMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskAuditMapper = taskAuditMapper;
        this.assessmentMapper = assessmentMapper;
        this.inventoryCheckMapper = inventoryCheckMapper;
        this.healthMapper = healthMapper;
        this.scoreMapper = scoreMapper;
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
            // 计算排名
            updateRankings(scoreMonth);
            String msg = "门店评分完成，已计算 " + count + " 家门店 (" + scoreMonth + ")";
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("门店评分计算失败", e);
            XxlJobHelper.handleFail("门店评分计算失败: " + e.getMessage());
        }
    }

    /**
     * 计算单个门店月度评分 — 全部使用 BigDecimal
     */
    private void calcStoreScore(Long storeId, String scoreMonth) {
        String nextMonth = nextMonth(scoreMonth);

        // ---- 纪律分 (10分) ----
        BigDecimal disciplineScore = calcDisciplineScore(storeId, scoreMonth, nextMonth);

        // ---- 人效分 (35分) = avg(total_score) / 100 * 35 ----
        BigDecimal humanScore = calcHumanScore(storeId, scoreMonth, nextMonth);

        // ---- 货品分 (30分) = (1 - abnormal/total) * 30 ----
        BigDecimal productScore = calcProductScore(storeId, scoreMonth, nextMonth);

        // ---- 场景分 (25分) = (1 - rectification/total) * 25 ----
        BigDecimal sceneScore = calcSceneScore(storeId, scoreMonth, nextMonth);

        BigDecimal totalScore = humanScore.add(productScore).add(sceneScore).add(disciplineScore)
                .setScale(2, RoundingMode.HALF_UP);

        // ---- 持久化到 store_monthly_score ----
        StoreMonthlyScore score = new StoreMonthlyScore();
        score.setStoreId(storeId);
        score.setScoreMonth(scoreMonth);
        score.setTotalScore(totalScore);
        score.setHumanScore(humanScore);
        score.setProductScore(productScore);
        score.setSceneScore(sceneScore);
        score.setDisciplineScore(disciplineScore);
        score.setOverdueCount(calcOverdueCount(storeId, scoreMonth, nextMonth));
        score.setRejectedCount(calcRejectedCount(storeId, scoreMonth, nextMonth));
        scoreMapper.insert(score);

        log.debug("门店[{}]评分入库: 总分={}, 人效={}, 货品={}, 场景={}, 纪律={}",
                storeId, totalScore, humanScore, productScore, sceneScore, disciplineScore);
    }

    // ==================== 四个维度 ====================

    /** 纪律分: 10 - 超时×0.5 - 驳回×0.5, 最低0 */
    private BigDecimal calcDisciplineScore(Long storeId, String month, String nextMonth) {
        long overdueCount = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getStoreId, storeId)
                        .eq(TaskInstance::getIsOverdue, 1)
                        .ge(TaskInstance::getCreatedAt, month + "-01")
                        .lt(TaskInstance::getCreatedAt, nextMonth + "-01"));

        long rejectedCount = taskAuditMapper.selectCount(
                new LambdaQueryWrapper<TaskAudit>()
                        .eq(TaskAudit::getAuditResult, "REJECTED")
                        .ge(TaskAudit::getAuditedAt, month + "-01")
                        .lt(TaskAudit::getAuditedAt, nextMonth + "-01")
                        .apply("task_id IN (SELECT id FROM task_instance WHERE store_id = {0})", storeId));

        BigDecimal deduction = BigDecimal.valueOf(overdueCount)
                .add(BigDecimal.valueOf(rejectedCount))
                .multiply(BigDecimal.valueOf(0.5));
        return BigDecimal.TEN.subtract(deduction).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /** 人效分: 本月员工考核平均分 / 100 * 35 */
    private BigDecimal calcHumanScore(Long storeId, String month, String nextMonth) {
        try {
            List<EmployeeAssessment> assessments = assessmentMapper.selectList(
                    new LambdaQueryWrapper<EmployeeAssessment>()
                            .ge(EmployeeAssessment::getCreatedAt, month + "-01")
                            .lt(EmployeeAssessment::getCreatedAt, nextMonth + "-01"));
            if (assessments.isEmpty()) return BigDecimal.ZERO;

            BigDecimal avgScore = assessments.stream()
                    .map(a -> a.getTotalScore() != null ? a.getTotalScore() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(assessments.size()), 4, RoundingMode.HALF_UP);
            return avgScore.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(35)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("人效分查询异常 storeId={}: {}", storeId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /** 货品分: (1 - 盘点异常率) * 30 */
    private BigDecimal calcProductScore(Long storeId, String month, String nextMonth) {
        try {
            List<ProductInventoryCheck> checks = inventoryCheckMapper.selectList(
                    new LambdaQueryWrapper<ProductInventoryCheck>()
                            .eq(ProductInventoryCheck::getStoreId, storeId)
                            .ge(ProductInventoryCheck::getCheckDate, month + "-01")
                            .lt(ProductInventoryCheck::getCheckDate, nextMonth + "-01"));
            if (checks.isEmpty()) return BigDecimal.ZERO;

            long totalChecked = checks.stream()
                    .mapToLong(c -> c.getTotalCheckedCount() != null ? c.getTotalCheckedCount() : 0).sum();
            long totalAbnormal = checks.stream()
                    .mapToLong(c -> c.getAbnormalCount() != null ? c.getAbnormalCount() : 0).sum();

            if (totalChecked == 0) return BigDecimal.ZERO;
            BigDecimal abnormalRate = BigDecimal.valueOf(totalAbnormal)
                    .divide(BigDecimal.valueOf(totalChecked), 4, RoundingMode.HALF_UP);
            return BigDecimal.ONE.subtract(abnormalRate).max(BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(30)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("货品分查询异常 storeId={}: {}", storeId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /** 场景分: (1 - 卫生需整改率) * 25 */
    private BigDecimal calcSceneScore(Long storeId, String month, String nextMonth) {
        try {
            List<SceneHealthInspection> inspections = healthMapper.selectList(
                    new LambdaQueryWrapper<SceneHealthInspection>()
                            .eq(SceneHealthInspection::getStoreId, storeId)
                            .ge(SceneHealthInspection::getInspectionDate, month + "-01")
                            .lt(SceneHealthInspection::getInspectionDate, nextMonth + "-01"));
            if (inspections.isEmpty()) return BigDecimal.ZERO;

            long total = inspections.size();
            long needRectification = inspections.stream()
                    .filter(h -> h.getRectificationRequired() != null && h.getRectificationRequired() == 1).count();

            BigDecimal rectRate = BigDecimal.valueOf(needRectification)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            return BigDecimal.ONE.subtract(rectRate).max(BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(25)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("场景分查询异常 storeId={}: {}", storeId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ==================== 辅助 ====================

    private int calcOverdueCount(Long storeId, String month, String nextMonth) {
        return Math.toIntExact(taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getStoreId, storeId)
                        .eq(TaskInstance::getIsOverdue, 1)
                        .ge(TaskInstance::getCreatedAt, month + "-01")
                        .lt(TaskInstance::getCreatedAt, nextMonth + "-01")));
    }

    private int calcRejectedCount(Long storeId, String month, String nextMonth) {
        return Math.toIntExact(taskAuditMapper.selectCount(
                new LambdaQueryWrapper<TaskAudit>()
                        .eq(TaskAudit::getAuditResult, "REJECTED")
                        .ge(TaskAudit::getAuditedAt, month + "-01")
                        .lt(TaskAudit::getAuditedAt, nextMonth + "-01")
                        .apply("task_id IN (SELECT id FROM task_instance WHERE store_id = {0})", storeId)));
    }

    /** 更新当月所有门店的排名 */
    private void updateRankings(String month) {
        List<StoreMonthlyScore> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<StoreMonthlyScore>()
                        .eq(StoreMonthlyScore::getScoreMonth, month)
                        .orderByDesc(StoreMonthlyScore::getTotalScore));
        int rank = 1;
        for (StoreMonthlyScore s : scores) {
            s.setRanking(rank++);
            scoreMapper.updateById(s);
        }
    }

    private String nextMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4));
        int m = Integer.parseInt(ym.substring(5, 7));
        if (m == 12) return (y + 1) + "-01";
        return y + "-" + String.format("%02d", m + 1);
    }
}
