package com.zhubao.manage.module.task.service;

import com.zhubao.manage.module.notification.service.NotificationService;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskReminderLog;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskReminderLogMapper;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务提醒服务 —— 根据 reminder_rule 写入 task_reminder_log
 *
 * 提醒类型:
 *   START     - 任务开始时提醒
 *   DEADLINE_2H  - 截止前2小时
 *   DEADLINE_30M - 截止前30分钟
 *   OVERDUE   - 已超时
 *   OVERDUE_24H - 超时24小时
 *
 * 此服务由 XXL-JOB / Scheduler 定时调用来扫描并生成提醒记录
 */
@Service
public class TaskReminderService {

    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskReminderLogMapper reminderLogMapper;
    private final NotificationService notificationService;

    public TaskReminderService(TaskInstanceMapper taskInstanceMapper,
                               TaskTemplateMapper taskTemplateMapper,
                               TaskReminderLogMapper reminderLogMapper,
                               NotificationService notificationService) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskTemplateMapper = taskTemplateMapper;
        this.reminderLogMapper = reminderLogMapper;
        this.notificationService = notificationService;
    }

    /**
     * 为单个任务生成提醒日志
     */
    @Transactional
    public void createReminder(Long taskId, String reminderType) {
        TaskInstance task = taskInstanceMapper.selectById(taskId);
        if (task == null) return;

        TaskReminderLog log = new TaskReminderLog();
        log.setTaskId(taskId);
        log.setReminderType(reminderType);
        log.setChannel("STATION");
        log.setReceiverId(task.getAssigneeId());
        log.setSendStatus("PENDING");
        log.setCreatedAt(LocalDateTime.now());
        reminderLogMapper.insert(log);

        // 同时写入 notification 表（前端铃铛/消息列表可见）
        String title = buildReminderTitle(reminderType, task.getTaskTitle());
        String content = "任务 \"" + task.getTaskTitle() + "\" " + getReminderDesc(reminderType);
        notificationService.send(task.getAssigneeId(), title, content, "TASK_REMIND", "TASK", taskId);
    }

    private String buildReminderTitle(String type, String taskTitle) {
        switch (type) {
            case "DEADLINE_2H": return "任务即将截止: " + taskTitle;
            case "DEADLINE_30M": return "任务即将截止(30分钟): " + taskTitle;
            case "OVERDUE": return "任务已超时: " + taskTitle;
            default: return "任务提醒: " + taskTitle;
        }
    }

    private String getReminderDesc(String type) {
        switch (type) {
            case "DEADLINE_2H": return "截止时间还剩2小时";
            case "DEADLINE_30M": return "截止时间还剩30分钟";
            case "OVERDUE": return "已超过截止时间";
            default: return "请及时处理";
        }
    }

    /**
     * 根据模板的 reminder_rule 批量创建提醒
     * 由调度器调用
     */
    @Transactional
    public void batchCreateReminders(Long templateId, String reminderType) {
        TaskTemplate template = taskTemplateMapper.selectById(templateId);
        if (template == null) return;

        // 查找该模板下所有未完成的任务
        java.util.List<TaskInstance> tasks = taskInstanceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getTemplateId, templateId)
                        .notIn(TaskInstance::getStatus, "COMPLETED", "CANCELLED", "VOIDED"));

        for (TaskInstance task : tasks) {
            createReminder(task.getId(), reminderType);
        }
    }

    /**
     * 扫描超时任务并标记 + 写入提醒
     */
    @Transactional
    public int scanOverdueTasks() {
        int count = 0;
        java.util.List<TaskInstance> pendingTasks = taskInstanceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskInstance>()
                        .lt(TaskInstance::getDueTime, LocalDateTime.now())
                        .in(TaskInstance::getStatus, "PENDING", "READY", "IN_PROGRESS"));

        for (TaskInstance task : pendingTasks) {
            task.setStatus("OVERDUE");
            task.setIsOverdue(1);
            long minutes = java.time.Duration.between(task.getDueTime(), LocalDateTime.now()).toMinutes();
            task.setOverdueMinutes((int) minutes);
            taskInstanceMapper.updateById(task);
            createReminder(task.getId(), "OVERDUE");
            count++;
        }
        return count;
    }
}
