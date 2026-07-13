package com.zhubao.manage.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.service.TaskService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务生成Job —— 周期任务自动生成
 *
 * 调度配置（XXL-JOB 管理台）：
 *   dailyTaskGenerateHandler   → cron: 0 0 1 * * ?      (每天凌晨1:00)
 *   weeklyTaskGenerateHandler  → cron: 0 0 2 ? * MON    (每周一凌晨2:00)
 *   monthlyTaskGenerateHandler → cron: 0 0 3 * * ?       (每天凌晨3:00，方法内判断15/20/25日)
 */
@Component
public class TaskGenerateJob {

    private static final Logger log = LoggerFactory.getLogger(TaskGenerateJob.class);

    private final TaskService taskService;
    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final StoreMapper storeMapper;

    public TaskGenerateJob(TaskService taskService,
                           TaskTemplateMapper taskTemplateMapper,
                           TaskInstanceMapper taskInstanceMapper,
                           StoreMapper storeMapper) {
        this.taskService = taskService;
        this.taskTemplateMapper = taskTemplateMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.storeMapper = storeMapper;
    }

    /**
     * 每日任务生成 —— DAILY 频率
     */
    @XxlJob("dailyTaskGenerateHandler")
    public void dailyTaskGenerateHandler() {
        log.info("===== 每日任务生成开始 =====");
        int total = 0;
        try {
            List<TaskTemplate> templates = taskTemplateMapper.selectList(
                    new LambdaQueryWrapper<TaskTemplate>()
                            .eq(TaskTemplate::getStatus, "ENABLED")
                            .eq(TaskTemplate::getFrequencyType, "DAILY"));

            for (TaskTemplate tmpl : templates) {
                List<Long> storeIds = filterDeduped(tmpl, resolveStoreIds(tmpl));
                if (!storeIds.isEmpty()) {
                    taskService.generateTasks(tmpl.getId(), storeIds);
                    total += storeIds.size();
                }
            }
            XxlJobHelper.handleSuccess("生成每日任务完成，共 " + total + " 条");
        } catch (Exception e) {
            log.error("每日任务生成失败", e);
            XxlJobHelper.handleFail("每日任务生成失败: " + e.getMessage());
        }
    }

    /**
     * 每周任务生成 —— WEEKLY 频率
     */
    @XxlJob("weeklyTaskGenerateHandler")
    public void weeklyTaskGenerateHandler() {
        log.info("===== 每周任务生成开始 =====");
        int total = 0;
        try {
            List<TaskTemplate> templates = taskTemplateMapper.selectList(
                    new LambdaQueryWrapper<TaskTemplate>()
                            .eq(TaskTemplate::getStatus, "ENABLED")
                            .eq(TaskTemplate::getFrequencyType, "WEEKLY"));

            for (TaskTemplate tmpl : templates) {
                List<Long> storeIds = filterDeduped(tmpl, resolveStoreIds(tmpl));
                if (!storeIds.isEmpty()) {
                    taskService.generateTasks(tmpl.getId(), storeIds);
                    total += storeIds.size();
                }
            }
            XxlJobHelper.handleSuccess("生成每周任务完成，共 " + total + " 条");
        } catch (Exception e) {
            log.error("每周任务生成失败", e);
            XxlJobHelper.handleFail("每周任务生成失败: " + e.getMessage());
        }
    }

    /**
     * 月度任务生成 —— MONTHLY/QUARTERLY 频率
     * 每天3:00触发，仅日期为 15/20/25 时执行（月度任务），季度末执行季度任务
     */
    @XxlJob("monthlyTaskGenerateHandler")
    public void monthlyTaskGenerateHandler() {
        int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
        if (dayOfMonth != 15 && dayOfMonth != 20 && dayOfMonth != 25) {
            XxlJobHelper.handleSuccess("非月度任务生成日(15/20/25)，跳过");
            return;
        }

        log.info("===== 月度任务生成开始 (day={}) =====", dayOfMonth);
        int total = 0;
        try {
            List<TaskTemplate> templates = taskTemplateMapper.selectList(
                    new LambdaQueryWrapper<TaskTemplate>()
                            .eq(TaskTemplate::getStatus, "ENABLED")
                            .in(TaskTemplate::getFrequencyType, "MONTHLY", "QUARTERLY"));

            for (TaskTemplate tmpl : templates) {
                List<Long> storeIds = filterDeduped(tmpl, resolveStoreIds(tmpl));
                if (!storeIds.isEmpty()) {
                    taskService.generateTasks(tmpl.getId(), storeIds);
                    total += storeIds.size();
                }
            }
            XxlJobHelper.handleSuccess("生成月度任务完成，共 " + total + " 条");
        } catch (Exception e) {
            log.error("月度任务生成失败", e);
            XxlJobHelper.handleFail("月度任务生成失败: " + e.getMessage());
        }
    }

    // ---- 内部 ----

    /** 去重：已存在同模板+同门店+当天日期的任务则跳过 (P2-15) */
    private List<Long> filterDeduped(TaskTemplate tmpl, List<Long> storeIds) {
        String today = java.time.LocalDate.now().toString();
        return storeIds.stream().filter(sid -> {
            Long count = taskInstanceMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskInstance>()
                            .eq(TaskInstance::getTemplateId, tmpl.getId())
                            .eq(TaskInstance::getStoreId, sid)
                            .ge(TaskInstance::getCreatedAt, today + " 00:00:00"));
            return count == 0;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 解析模板适用门店ID列表
     */
    private List<Long> resolveStoreIds(TaskTemplate tmpl) {
        // 从 applicable_store_ids 解析
        String ids = tmpl.getApplicableStoreIds();
        if (ids != null && !ids.trim().isEmpty()) {
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        // 从 applicable_region_ids 查门店
        String regionIds = tmpl.getApplicableRegionIds();
        if (regionIds != null && !regionIds.trim().isEmpty()) {
            List<Long> rids = Arrays.stream(regionIds.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .map(Long::valueOf).collect(Collectors.toList());
            return storeMapper.selectList(
                    new LambdaQueryWrapper<Store>().in(Store::getRegionId, rids))
                    .stream().map(Store::getId).collect(Collectors.toList());
        }
        // 全部门店
        return storeMapper.selectList(null).stream()
                .map(Store::getId).collect(Collectors.toList());
    }
}
