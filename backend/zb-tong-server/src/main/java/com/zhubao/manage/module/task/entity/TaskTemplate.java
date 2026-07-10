package com.zhubao.manage.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_template")
public class TaskTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;
    private Long actionId;
    private String dimension;
    private String category;
    private String description;
    private String executionStandard;
    private Integer requiredPhotos;
    private Integer requiredText;
    private Integer requiredForm;
    private Long formSchemaId;
    private Integer requireAudit;
    private String defaultAuditorRole;
    private String frequencyType;
    private String cronExpression;
    private String dueTimeRule;
    private String reminderRule;
    private BigDecimal scoreWeight;
    private Integer isDefault;
    private Integer isForce;
    private String applicableStoreIds;
    private String applicableRegionIds;
    private String status;
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
