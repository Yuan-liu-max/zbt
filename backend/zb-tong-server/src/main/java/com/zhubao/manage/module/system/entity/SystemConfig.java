package com.zhubao.manage.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configValue;

    private String configGroup;

    private String description;

    private String status = "ENABLED";

    private Integer sortOrder = 0;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
