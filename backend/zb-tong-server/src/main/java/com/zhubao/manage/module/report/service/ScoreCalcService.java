package com.zhubao.manage.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评分计算引擎
 *
 * 总分 100 = 人效分(35) + 货品分(30) + 场景分(25) + 纪律分(10)
 *
 * 纪律分细则:
 *   - 基础10分
 *   - 超时一次 -0.5
 *   - 驳回一次 -0.5
 *   - 最低0分
 *
 * TODO: 人效/货品/场景评分细则待接入对应模块数据后完善
 */
@Service
public class ScoreCalcService {

    private final TaskInstanceMapper taskInstanceMapper;

    public ScoreCalcService(TaskInstanceMapper taskInstanceMapper) {
        this.taskInstanceMapper = taskInstanceMapper;
    }

    public Map<String, Object> calcStoreScore(Long storeId, String scoreMonth) {
        String nextMonth = nextMonth(scoreMonth);

        // ---- 纪律分 ----
        long overdueCount = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getStoreId, storeId)
                        .eq(TaskInstance::getIsOverdue, 1)
                        .ge(TaskInstance::getCreatedAt, scoreMonth + "-01")
                        .lt(TaskInstance::getCreatedAt, nextMonth + "-01"));

        long rejectedCount = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getStoreId, storeId)
                        .eq(TaskInstance::getStatus, "REJECTED")
                        .ge(TaskInstance::getCreatedAt, scoreMonth + "-01")
                        .lt(TaskInstance::getCreatedAt, nextMonth + "-01"));

        BigDecimal disciplineScore = BigDecimal.valueOf(Math.max(0,
                10 - overdueCount * 0.5 - rejectedCount * 0.5)).setScale(2, RoundingMode.HALF_UP);

        // ---- 其他维度（默认满分，待接入） ----
        BigDecimal humanScore = BigDecimal.valueOf(35);
        BigDecimal productScore = BigDecimal.valueOf(30);
        BigDecimal sceneScore = BigDecimal.valueOf(25);

        // TODO: humanScore = calcEmployeePerformance(storeId, scoreMonth) * 35/100
        // TODO: productScore = calcProductMgmt(storeId, scoreMonth) * 30/100
        // TODO: sceneScore = calcSceneMgmt(storeId, scoreMonth) * 25/100

        BigDecimal total = humanScore.add(productScore).add(sceneScore).add(disciplineScore)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("storeId", storeId);
        result.put("scoreMonth", scoreMonth);
        result.put("totalScore", total);
        result.put("humanScore", humanScore);
        result.put("productScore", productScore);
        result.put("sceneScore", sceneScore);
        result.put("disciplineScore", disciplineScore);
        result.put("overdueCount", overdueCount);
        result.put("rejectedCount", rejectedCount);
        return result;
    }

    private String nextMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4));
        int m = Integer.parseInt(ym.substring(5, 7));
        if (m == 12) return (y + 1) + "-01";
        return y + "-" + String.format("%02d", m + 1);
    }
}
