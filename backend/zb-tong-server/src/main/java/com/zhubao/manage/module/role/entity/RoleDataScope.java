package com.zhubao.manage.module.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_data_scope")
public class RoleDataScope {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private String scopeType;

    private Long scopeValue;
}
