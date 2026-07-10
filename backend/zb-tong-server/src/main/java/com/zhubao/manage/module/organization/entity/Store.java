package com.zhubao.manage.module.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_store")
public class Store {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orgId;

    private String storeName;

    private String storeCode;

    private Long regionId;

    private String address;

    private Long storeManagerId;

    private LocalDate openingDate;

    private String storeType;

    private String status;

    private String businessHours;

    private String contactPhone;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
