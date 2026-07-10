package com.zhubao.manage.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_reminder_log")
public class TaskReminderLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private String reminderType;
    private String channel;
    private Long receiverId;
    private String sendStatus;
    private LocalDateTime sentAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
