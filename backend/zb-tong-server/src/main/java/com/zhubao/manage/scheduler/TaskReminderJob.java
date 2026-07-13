package com.zhubao.manage.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskReminderLog;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskReminderLogMapper;
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
 * 任务提醒Job —— 每10分钟扫描，消除提醒空窗
 */
@Component
public class TaskReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TaskReminderJob.class);

    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskReminderService taskReminderService;
    private final TaskReminderLogMapper reminderLogMapper;

    public TaskReminderJob(TaskTemplateMapper ttm, TaskInstanceMapper tim,
                           TaskReminderService trs, TaskReminderLogMapper rlm) {
        this.taskTemplateMapper = ttm;
        this.taskInstanceMapper = tim;
        this.taskReminderService = trs;
        this.reminderLogMapper = rlm;
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
            XxlJobHelper.handleSuccess("提醒扫描完成，生成提醒 " + total + " 条");
        } catch (Exception e) {
            log.error("提醒扫描失败", e);
            XxlJobHelper.handleFail("提醒扫描失败: " + e.getMessage());
        }
    }

    private int processTemplateReminders(TaskTemplate tmpl) {
        int count = 0;
        List<TaskInstance> tasks = taskInstanceMapper.selectList(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getTemplateId, tmpl.getId())
                        .in(TaskInstance::getStatus, "PENDING", "READY", "IN_PROGRESS", "SUBMITTED", "AUDITING"));

        LocalDateTime now = LocalDateTime.now();
        for (TaskInstance task : tasks) {
            if (task.getDueTime() == null) continue;

            long minutesUntilDue = ChronoUnit.MINUTES.between(now, task.getDueTime());

            if (minutesUntilDue < 0) continue; // 已超时，由 TaskOverdueJob 处理

            // DEADLINE_2H: 覆盖 120min 到 31min 之间 (P2-14 + fix)
            if (minutesUntilDue <= 120 && minutesUntilDue > 30) {
                if (tryCreateReminder(task.getId(), "DEADLINE_2H")) count++;
            }
            // DEADLINE_30M: 覆盖 30min 到 0min 之间
            else if (minutesUntilDue <= 30 && minutesUntilDue >= 0) {
                if (tryCreateReminder(task.getId(), "DEADLINE_30M")) count++;
            }
        }
        return count;
    }

    /**
     * 10分钟内已生成过同类型提醒则跳过（去重）
     */
    private boolean tryCreateReminder(Long taskId, String reminderType) {
        Long recentCount = reminderLogMapper.selectCount(
                new LambdaQueryWrapper<TaskReminderLog>()
                        .eq(TaskReminderLog::getTaskId, taskId)
                        .eq(TaskReminderLog::getReminderType, reminderType)
                        .ge(TaskReminderLog::getCreatedAt, LocalDateTime.now().minusMinutes(10)));
        if (recentCount > 0) {
            log.debug("10分钟内已有同类型提醒，跳过: taskId={}, type={}", taskId, reminderType);
            return false;
        }
        taskReminderService.createReminder(taskId, reminderType);
        return true;
    }
}
