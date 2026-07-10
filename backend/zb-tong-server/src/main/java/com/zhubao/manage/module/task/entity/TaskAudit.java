package com.zhubao.manage.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_audit")
public class TaskAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long submissionId;
    private Long auditorId;
    private String auditResult;
    private String auditComment;
    private BigDecimal score;
    private LocalDateTime auditedAt;
}
