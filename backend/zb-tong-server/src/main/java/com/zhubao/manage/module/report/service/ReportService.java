package com.zhubao.manage.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.mapper.StoreMonthlyScoreMapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StoreMonthlyScoreMapper scoreMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final StoreMapper storeMapper;
    private final ScoreCalcService scoreCalcService;

    public ReportService(StoreMonthlyScoreMapper sm, TaskInstanceMapper tm,
                         StoreMapper stm, ScoreCalcService scs) {
        this.scoreMapper = sm; this.taskInstanceMapper = tm;
        this.storeMapper = stm; this.scoreCalcService = scs;
    }

    // ---- 门店评分 ----
    public IPage<StoreMonthlyScore> pageScores(PageDTO dto, String month) {
        LambdaQueryWrapper<StoreMonthlyScore> w = new LambdaQueryWrapper<>();
        if (month != null) w.eq(StoreMonthlyScore::getScoreMonth, month);
        w.orderByDesc(StoreMonthlyScore::getScoreMonth);
        return scoreMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public StoreMonthlyScore getScore(Long id) { return scoreMapper.selectById(id); }

    // ---- 门店排名 ----
    public List<Map<String, Object>> storeRanking(String month) {
        List<StoreMonthlyScore> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<StoreMonthlyScore>().eq(StoreMonthlyScore::getScoreMonth, month)
                        .orderByDesc(StoreMonthlyScore::getTotalScore));
        List<Store> stores = storeMapper.selectList(null);
        Map<Long, String> nameMap = stores.stream().collect(Collectors.toMap(Store::getId, Store::getStoreName));
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (StoreMonthlyScore s : scores) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ranking", rank++);
            m.put("storeId", s.getStoreId());
            m.put("storeName", nameMap.getOrDefault(s.getStoreId(), ""));
            m.put("totalScore", s.getTotalScore());
            m.put("humanScore", s.getHumanScore());
            m.put("productScore", s.getProductScore());
            m.put("sceneScore", s.getSceneScore());
            m.put("disciplineScore", s.getDisciplineScore());
            result.add(m);
        }
        return result;
    }

    // ---- 任务完成率 ----
    public Map<String, Object> taskCompletionReport(String month) {
        LambdaQueryWrapper<TaskInstance> w = new LambdaQueryWrapper<TaskInstance>()
                .ge(TaskInstance::getCreatedAt, month + "-01")
                .lt(TaskInstance::getCreatedAt, nextMonth(month) + "-01");
        List<TaskInstance> all = taskInstanceMapper.selectList(w);
        long total = all.size();
        long completed = all.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long overdue = all.stream().filter(t -> t.getIsOverdue() != null && t.getIsOverdue() == 1).count();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("completed", completed);
        m.put("completionRate", total == 0 ? "0%" : BigDecimal.valueOf(completed * 100.0 / total).setScale(1, RoundingMode.HALF_UP) + "%");
        m.put("overdue", overdue);
        m.put("overdueRate", total == 0 ? "0%" : BigDecimal.valueOf(overdue * 100.0 / total).setScale(1, RoundingMode.HALF_UP) + "%");
        return m;
    }

    // ---- 数据驾驶舱 ----
    public Map<String, Object> dashboard(String role, Long userId, Long storeId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", role);
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 通用数据
        long totalTasks = taskInstanceMapper.selectCount(new LambdaQueryWrapper<TaskInstance>());
        long completedTasks = taskInstanceMapper.selectCount(new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getStatus, "COMPLETED"));
        long overdueTasks = taskInstanceMapper.selectCount(new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getIsOverdue, 1));

        data.put("totalTasks", totalTasks);
        data.put("completedTasks", completedTasks);
        data.put("overdueTasks", overdueTasks);

        switch (role) {
            case "ROLE_ADMIN": case "ROLE_HQ":
                data.put("storeCount", storeMapper.selectCount(null));
                break;
            case "ROLE_ASSOCIATE":
                data.put("myTasks", taskInstanceMapper.selectCount(
                        new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getAssigneeId, userId)));
                break;
        }
        // TODO: 扩展各角色视图数据

        return data;
    }

    private String nextMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4)), m = Integer.parseInt(ym.substring(5, 7));
        if (m == 12) return (y + 1) + "-01";
        return y + "-" + String.format("%02d", m + 1);
    }
}
