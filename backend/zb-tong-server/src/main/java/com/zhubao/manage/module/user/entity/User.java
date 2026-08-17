package com.zhubao.manage.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String passwordHash;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private String timezone;

    private String language;

    private String dateFormat;

    private Boolean notifySystem;

    private Boolean notifyOrder;

    private Boolean notifyInventory;

    private Boolean notifyMarketing;

    private Long storeId;

    private Long regionId;

    private String position;

    private LocalDate entryDate;

    private String status;

    /** 令牌版本号，强制下线时+1，旧token立即失效 */
    private Integer tokenVersion;

    private LocalDateTime lastLoginAt;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
