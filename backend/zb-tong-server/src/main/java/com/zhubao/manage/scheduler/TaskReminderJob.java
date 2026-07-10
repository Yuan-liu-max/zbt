package com.zhubao.manage.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import com.zhubao.manage.module.task.service.TaskReminderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 任务提醒Job —— 每10分钟扫描 reminder_rule
 *
 * 调度配置: cron: 0 0/10 * * * ?
 *
 * 逻辑:
 *   1. 扫描所有启用模板的 reminder_rule
 *   2. 匹配时间窗口（截止前2h / 截止前30min / 超时 / 超时24h）
 *   3. 写入 task_reminder_log
 *   4. 发送通知（站内信/企业微信/短信 预留）
 */
@Component
public class TaskReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TaskReminderJob.class);

    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskReminderService taskReminderService;

    public TaskReminderJob(TaskTemplateMapper taskTemplateMapper,
                           TaskInstanceMapper taskInstanceMapper,
                           TaskReminderService taskReminderService) {
        this.taskTemplateMapper = taskTemplateMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskReminderService = taskReminderService;
    }

    @XxlJob("taskReminderHandler")
    public void taskReminderHandler() {
        log.info("===== 任务提醒扫描开始 =====");
        int total = 0;
        try {
            List<TaskTemplate> templates = taskTemplateMapper.selectList(
                    new LambdaQueryWrapper<TaskTemplate>()
                            .eq(TaskTemplate::getStatus, "ENABLED")
                            .isNotNull(TaskTemplate::getReminderRule));

            for (TaskTemplate tmpl : templates) {
                total += processTemplateReminders(tmpl);
            }
            String msg = "提醒扫描完成，生成提醒 " + total + " 条";
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("提醒扫描失败", e);
            XxlJobHelper.handleFail("提醒扫描失败: " + e.getMessage());
        }
    }

    private int processTemplateReminders(TaskTemplate tmpl) {
        int count = 0;
        // 查找该模板下所有活跃的任务
        List<TaskInstance> tasks = taskInstanceMapper.selectList(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getTemplateId, tmpl.getId())
                        .in(TaskInstance::getStatus, "PENDING", "READY", "IN_PROGRESS", "SUBMITTED", "AUDITING"));

        LocalDateTime now = LocalDateTime.now();
        for (TaskInstance task : tasks) {
            if (task.getDueTime() == null) continue;

            long hoursUntilDue = ChronoUnit.HOURS.between(now, task.getDueTime());

            // 截止前2小时提醒（包含2h到30min之间的所有任务）P2-14 fix
            if (hoursUntilDue >= 0 && hoursUntilDue <= 2) {
                taskReminderService.createReminder(task.getId(), "DEADLINE_2H");
                count++;
            }
            // 截止前30分钟提醒
            else if (hoursUntilDue >= 0 && hoursUntilDue <= 1) {
                long minutesUntilDue = ChronoUnit.MINUTES.between(now, task.getDueTime());
                if (minutesUntilDue >= 0 && minutesUntilDue <= 30) {
                    taskReminderService.createReminder(task.getId(), "DEADLINE_30M");
                    count++;
                }
            }
        }
        return count;
    }
}
