package com.zhubao.manage.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_instance")
public class TaskInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;
    private Long templateId;
    private String taskTitle;
    private String dimension;
    private String category;
    private Long storeId;
    private Long assigneeId;
    private Long auditorId;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime dueTime;
    private LocalDateTime completedTime;
    private String status;
    private String priority;
    private String sourceType;
    private String relatedObjectType;
    private Long relatedObjectId;
    private Integer isOverdue;
    private Integer overdueMinutes;
    private BigDecimal qualityScore;
    private BigDecimal aiScore;
    private BigDecimal manualScore;
    private BigDecimal finalScore;

    @TableLogic private Integer isDeleted;

    @TableField(exist = false) private String assigneeName;
    @TableField(exist = false) private String auditorName;

    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
