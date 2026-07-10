package com.zhubao.manage.scheduler;

import com.zhubao.manage.module.task.service.TaskReminderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 任务超时检查Job —— 每5分钟扫描
 *
 * 调度配置: cron: 0 0/5 * * * ?
 *
 * 逻辑:
 *   1. 查询 due_time 小于 NOW() 且状态为 PENDING/READY/IN_PROGRESS 的任务
 *   2. 更新状态为 OVERDUE
 *   3. 写入 task_reminder_log 通知执行人和店长
 */
@Component
public class TaskOverdueJob {

    private static final Logger log = LoggerFactory.getLogger(TaskOverdueJob.class);

    private final TaskReminderService taskReminderService;

    public TaskOverdueJob(TaskReminderService taskReminderService) {
        this.taskReminderService = taskReminderService;
    }

    @XxlJob("taskOverdueHandler")
    public void taskOverdueHandler() {
        log.info("===== 任务超时扫描开始 =====");
        try {
            int count = taskReminderService.scanOverdueTasks();
            String msg = "超时扫描完成，标记超时任务 " + count + " 条";
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("超时扫描失败", e);
            XxlJobHelper.handleFail("超时扫描失败: " + e.getMessage());
        }
    }
}
