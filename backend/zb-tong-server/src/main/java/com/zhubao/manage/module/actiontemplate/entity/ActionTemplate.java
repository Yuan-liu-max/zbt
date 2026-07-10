package com.zhubao.manage.module.actiontemplate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("action_template")
public class ActionTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String actionName;
    private String dimension;
    private String category;
    private String description;
    private String executionStandard;
    private String frequencyType;
    private String cronExpression;
    private String dueTimeRule;
    private Integer requiredPhotos;
    private Integer requiredText;
    private Integer requiredForm;
    private Long formSchemaId;
    private Integer requireAudit;
    private String defaultAuditorRole;
    private BigDecimal scoreWeight;
    private Integer isDefault;
    private Integer isForce;
    private String applicableStoreTypes;
    private String status;
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
