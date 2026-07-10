package com.zhubao.manage.module.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_organization")
public class Organization {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String orgName;

    private String orgType;

    private String orgCode;

    private Integer sortOrder;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
