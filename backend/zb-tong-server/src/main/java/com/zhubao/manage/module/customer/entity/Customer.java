package com.zhubao.manage.module.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO) private Long id;
    private String code;
    private String name;
    private String phone;
    private String level;
    private BigDecimal totalConsumption;
    private Integer points;
    private String registeredAt;
    private String lastConsumptionAt;
    private String status;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
